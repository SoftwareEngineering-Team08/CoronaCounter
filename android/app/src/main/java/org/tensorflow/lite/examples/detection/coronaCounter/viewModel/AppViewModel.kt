package com.example.coronacounter.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.coronacounter.model.Authenticator
import com.example.coronacounter.model.Shop
import com.example.coronacounter.model.User
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.examples.detection.coronaCounter.api.Api
import org.tensorflow.lite.examples.detection.coronaCounter.api.RetrofitInstance
import org.tensorflow.lite.examples.detection.coronaCounter.model.Trial

private const val TAG = "AppViewModel"

class AppViewModel:ViewModel(){
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user

    private val _shops = MutableLiveData<List<Shop>>()
    val shops: LiveData<List<Shop>> get() = _shops

    private val _shop = MutableLiveData<Shop>()
    val shop: LiveData<Shop> get() = _shop

    private val _mainShop = MutableLiveData<Shop>()
    val mainShop: LiveData<Shop> get() = _mainShop

    private val _mainStage = MutableLiveData<Int>()
    val mainStage: LiveData<Int> get() = _mainStage

    private val _stats = MutableLiveData<List<Trial>>()
    val stats: LiveData<List<Trial>> get() = _stats
    val statsEntry:List<BarEntry> get() {
        val entries = mutableListOf<BarEntry>()
        for (trial in stats.value!!){
            entries.add(BarEntry(trial.tid.toFloat(),trial.tnum.toFloat()))
        }
        return entries
    }


    val limitPeople: Int get() = _mainShop.value!!.limitPeople(_mainStage.value!!)
    val auth = Authenticator
//    val shopdata  =  Datas.shops

    private val Api = RetrofitInstance.instance.create(Api::class.java)

    suspend fun AddCountInfo(trial: Trial) : Boolean {
        return withContext(Dispatchers.IO){
            val DBAccess = Api.addCountNumber(trial)
            if (DBAccess.isSuccessful){
                val returnValue = DBAccess.body()!!
                returnValue
            }else{
                false
            }
        }
    }
    // ????????? ????????????, ????????? ???????????? ??????
    suspend fun signin(user: User): Boolean {
        return withContext(Dispatchers.IO){
            val DBAccess = Api.authentication(user)
            if (DBAccess.isSuccessful){ // http code
                val DBuserInfo = DBAccess.body()!!
                if (DBuserInfo.id.equals("id invalid")){
                    false
                } else if (DBuserInfo.pw.equals("password wrong")){
                    false
                } else {
                    _user.postValue(DBuserInfo)
                    true
                }
            } else{     // network error
                Log.d(TAG,"network error")
                false
            }
        }
    }
    // ??????????????? ????????????, ???????????? ????????? ??????????????? ??????
    suspend fun getDistance(rname: String): Integer{
        return withContext(Dispatchers.IO){
            val DBAccess = Api.getDistance(rname)
            if (DBAccess.isSuccessful){
                DBAccess.body()!!
            }
            DBAccess.body()!!
        }
    }
    // id??? ???????????? , ????????? ????????? ??? true??? ??????????????? ??????
    suspend fun isNewUser(id:String) : Boolean {
        return withContext(Dispatchers.IO){
            val DBAccess = Api.isIdValid(id)
            if (DBAccess.isSuccessful){ // http code
                val isValid = DBAccess.body()!!
                isValid
            } else{     // network error
                Log.d(TAG,"network error")
                false
            }
        }
    }
    // user??? ?????????, db??? ??????????????? ??????????????? return?????? ??????
    suspend fun addUser(user:User) : Boolean {
        return withContext(Dispatchers.IO){
            val DBAccess = Api.addUser(user)
            if (DBAccess.isSuccessful){ // http code
                val didSucceed = DBAccess.body()!!
                didSucceed
            } else{     // network error
                Log.d(TAG,"addUser network error")
                false
            }
        }
    }

    //????????? ???????????? ??????
    suspend fun getStatistic(shop: Shop)  {
        Log.d(TAG,"give me statistic"+shop.toString())
        return withContext(Dispatchers.IO) {
            val DBAccess = Api.getStatistic(shop)
            if (DBAccess.isSuccessful){ // http code
                Log.d(TAG,DBAccess.body()!!.toString())
                _stats.postValue(DBAccess.body()!!)

            } else{
                Log.d(TAG,"dbaccessfailed")
                // listOf<Trial>()
            }
        }
    }

    suspend fun fetchShops() {
        Log.d(TAG,"fetch shops")
        withContext(Dispatchers.IO) {
            //TODO ????????????????????? ??????
            val DBAcess = Api.getShopLists(_user.value!!)
//            _shops.postValue(shopdata[user.value?.id] ?: listOf<Shop>())
            _shops.postValue(DBAcess.body())
            Log.d(TAG,DBAcess.body().toString())
        }
    }

    //?????? ??????????????? ?????????????????? ??????
    suspend fun fetchStage() {
        Log.d(TAG,"fetch primary stage")
        withContext(Dispatchers.IO) {
            //TODO ????????????????????? ??????
            val DBAcess = Api.getDistance(_mainShop.value!!.location!!)
//            _shops.postValue(shopdata[user.value?.id] ?: listOf<Shop>())
            _mainStage.postValue(DBAcess.body()!!.toInt())
            Log.d(TAG,DBAcess.body().toString())
        }
    }

    // ????????? =  ???????????? ??? ????????? ??????.
    fun setPrimaryShop(shop:Shop){
        _mainShop.value = shop
    }
    // shop??? ?????????, db??? ??????????????? ??????????????? return?????? ??????
    suspend fun addShop(shop:Shop) : Boolean {
        return withContext(Dispatchers.IO){
            val DBAccess = Api.addShop(shop)
            if (DBAccess.isSuccessful){ // http code
                val didSucceed = DBAccess.body()!!
                didSucceed
            } else{     // network error
                Log.d(TAG,"shop network error")
                false
            }
        }
    }

    // shop??? ?????????, db?????? ???????????? ??? ??????????????? ??????
    suspend fun deleteShop(shop:Shop) : Boolean {
        return withContext(Dispatchers.IO){
            val DBAccess = Api.deleteShop(shop)
            if (DBAccess.isSuccessful){ // http code
                val didSucceed = DBAccess.body()!!
                didSucceed
            } else{     // network error
                Log.d(TAG,"delete shop network error")
                false
            }
        }
    }

    // sid??? shop??? ?????????, db?????? ?????????????????? ??? ??????????????? ??????
    suspend fun editShop(shopPair:Map<String,Shop>) : Boolean {
        return withContext(Dispatchers.IO){
            val DBAccess = Api.editShop(shopPair)
            if (DBAccess.isSuccessful){ // http code
                val didSucceed = DBAccess.body()!!
                didSucceed
            } else{     // network error
                Log.d(TAG,"edit shop network error")
                false
            }
        }
    }
}
