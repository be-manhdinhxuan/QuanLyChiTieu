package com.example.quanlychitieu.presentation.features.search.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quanlychitieu.domain.model.filter.Filter;
import com.example.quanlychitieu.domain.model.spending.Spending;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SearchViewModel extends ViewModel {
    private final MutableLiveData<List<Spending>> spendingList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // Các chỉ số trong danh sách chooseIndex
    private static final int MONEY_INDEX = 0;
    private static final int TIME_INDEX = 1;
    private static final int TYPE_INDEX = 2;

    public LiveData<List<Spending>> getSpendingList() {
        return spendingList;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void search(String query, Filter filter) {
        if (query == null || query.isEmpty()) {
            spendingList.setValue(new ArrayList<>());
            return;
        }

        isLoading.setValue(true);

        FirebaseFirestore.getInstance()
                .collection("data")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> spendingIds = new ArrayList<>();
                        for (Object value : documentSnapshot.getData().values()) {
                            if (value instanceof List) {
                                for (Object id : (List) value) {
                                    spendingIds.add(id.toString());
                                }
                            }
                        }
                        loadSpendingList(spendingIds, query, filter);
                    } else {
                        isLoading.setValue(false);
                        spendingList.setValue(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    spendingList.setValue(new ArrayList<>());
                });
    }

    private void loadSpendingList(List<String> spendingIds, String query, Filter filter) {
        List<Spending> results = new ArrayList<>();

        // Nếu không có ID nào, kết thúc tìm kiếm ngay
        if (spendingIds.isEmpty()) {
            isLoading.setValue(false);
            spendingList.setValue(results);
            return;
        }

        // Đếm số lượng request đã hoàn thành
        final int[] completedRequests = { 0 };
        final int totalRequests = spendingIds.size();

        for (String id : spendingIds) {
            FirebaseFirestore.getInstance()
                    .collection("spendings")
                    .document(id)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            Spending spending = document.toObject(Spending.class);
                            if (spending != null && checkMatchQuery(spending, query, filter)) {
                                results.add(spending);
                            }
                        }

                        completedRequests[0]++;
                        if (completedRequests[0] >= totalRequests) {
                            isLoading.setValue(false);
                            spendingList.setValue(results);
                        }
                    })
                    .addOnFailureListener(e -> {
                        completedRequests[0]++;
                        if (completedRequests[0] >= totalRequests) {
                            isLoading.setValue(false);
                            spendingList.setValue(results);
                        }
                    });
        }
    }

    private boolean checkMatchQuery(Spending spending, String query, Filter filter) {
        // Kiểm tra query
        if (spending.getTypeName() == null || !spending.getTypeName().toUpperCase().contains(query.toUpperCase())) {
            return false;
        }

        List<Integer> chooseIndex = filter.getChooseIndex();
        // Kiểm tra xem danh sách chooseIndex có đủ các phần tử không
        if (chooseIndex != null && chooseIndex.size() > MONEY_INDEX) {
            int moneyIndex = chooseIndex.get(MONEY_INDEX);
            // Kiểm tra các điều kiện filter tiền
            if (moneyIndex == 1 && Math.abs(spending.getMoney()) < filter.getMoney()) {
                return false;
            } else if (moneyIndex == 2 && Math.abs(spending.getMoney()) > filter.getMoney()) {
                return false;
            } else if (moneyIndex == 3 && (Math.abs(spending.getMoney()) > filter.getFinishMoney() ||
                    Math.abs(spending.getMoney()) < filter.getMoney())) {
                return false;
            } else if (moneyIndex == 4 && Math.abs(spending.getMoney()) == filter.getMoney()) {
                return false;
            }
        }

        if (chooseIndex != null && chooseIndex.size() > TIME_INDEX && filter.getTime() != null) {
            int timeIndex = chooseIndex.get(TIME_INDEX);
            // Kiểm tra các điều kiện filter thời gian
            if (timeIndex == 1 && filter.getTime().after(spending.getDateTime())) {
                return false;
            } else if (timeIndex == 2 && filter.getTime().before(spending.getDateTime())) {
                return false;
            } else if (timeIndex == 3 && filter.getFinishTime() != null &&
                    (spending.getDateTime().after(filter.getFinishTime()) ||
                            spending.getDateTime().before(filter.getTime()))) {
                return false;
            } else if (timeIndex == 4 && isSameDay(spending.getDateTime(), filter.getTime())) {
                return false;
            }
        }

        if (chooseIndex != null && chooseIndex.size() > TYPE_INDEX) {
            int typeIndex = chooseIndex.get(TYPE_INDEX);
            // Kiểm tra các điều kiện filter loại
            if (typeIndex == 1 && spending.getMoney() < 0) {
                return false;
            } else if (typeIndex == 2 && spending.getMoney() > 0) {
                return false;
            }
        }

        if (filter.getFriends() != null && !filter.getFriends().isEmpty()) {
            boolean hasFriend = false;
            for (String friend : filter.getFriends()) {
                if (spending.getFriends() != null && spending.getFriends().contains(friend)) {
                    hasFriend = true;
                    break;
                }
            }
            if (!hasFriend) {
                return false;
            }
        }

        if (filter.getNote() != null && !filter.getNote().isEmpty() &&
                (spending.getNote() == null || !spending.getNote().contains(filter.getNote()))) {
            return false;
        }

        return true;
    }

    private boolean isSameDay(java.util.Date date1, java.util.Date date2) {
        java.util.Calendar cal1 = java.util.Calendar.getInstance();
        java.util.Calendar cal2 = java.util.Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR);
    }
}