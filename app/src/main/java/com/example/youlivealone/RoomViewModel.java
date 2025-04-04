package com.example.youlivealone;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Arrays;
import java.util.List;

public class RoomViewModel extends ViewModel {

    final MutableLiveData<List<String>> roomList;
    final MutableLiveData<List<String>> roomList1;

    public RoomViewModel() {
        roomList = new MutableLiveData<>();
        roomList1 = new MutableLiveData<>();
        loadRooms();
    }

    public LiveData<List<String>> getRoomList() {
        return roomList;
    }

    public LiveData<List<String>> getRoomList1() {
        return roomList1;
    }

    private void loadRooms() {
        // 추후 서버에서 데이터를 가져오는 부분을 구현
        List<String> dummyRooms = Arrays.asList("Room 1", "Room 2", "Room 3", "Room 4");
        List<String> dummyRooms1 = Arrays.asList("Room 1", "Room 2", "Room 3", "Room 4", "Room 5", "Room 6");
        roomList.setValue(dummyRooms);
        roomList1.setValue(dummyRooms1);
    }
}