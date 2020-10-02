package indi.hitszse2020g6.wakeapp.mainPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import indi.hitszse2020g6.wakeapp.EventTableEntry
import indi.hitszse2020g6.wakeapp.RoomDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainPageListViewModel: ViewModel() {
    public lateinit var eventList: LiveData<List<EventTableEntry>>
    private lateinit var DAO: RoomDAO

    fun init(dao: RoomDAO) {
        DAO = dao
        viewModelScope.launch(Dispatchers.IO) {
            eventList = DAO.getEvents()
        }
    }

    fun getEEventList(): LiveData<List<EventTableEntry>> {
        return eventList
    }
}