package com.example.quanlychitieu.views.calendar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quanlychitieu.domain.model.spending.Spending;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CalendarViewModel extends ViewModel {
    private final FirebaseFirestore firestore;

    public CalendarViewModel() {
        firestore = FirebaseFirestore.getInstance();
    }

    public LiveData<List<Spending>> loadSpendingList(List<String> idList) {
        MutableLiveData<List<Spending>> result = new MutableLiveData<>();

        if (idList == null || idList.isEmpty()) {
            result.setValue(new ArrayList<>());
            return result;
        }

        List<Spending> spendingList = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(idList.size());

        for (String id : idList) {
            firestore.collection("spending")
                    .document(id)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            try {
                                // Sử dụng phương thức fromFirestore đã cập nhật
                                Spending spending = Spending.fromFirestore(document);
                                if (spending != null) {
                                    spendingList.add(spending);
                                }
                            } catch (Exception e) {
                                // Xử lý lỗi khi chuyển đổi
                                e.printStackTrace();
                            }
                        }

                        if (counter.decrementAndGet() == 0) {
                            result.setValue(spendingList);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (counter.decrementAndGet() == 0) {
                            result.setValue(spendingList);
                        }
                    });
        }

        return result;
    }
}