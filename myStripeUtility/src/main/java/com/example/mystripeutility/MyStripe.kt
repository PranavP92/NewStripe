package com.example.mystripeutility

import StripeUtils
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.coroutines.*

class MyStripe(private val STRIPE_PUBLISHABLE_KEY: String, private val STRIPE_SECRET_KEY: String) {

    private val MY_STRIPE_PUBLISHABLE_KEY = STRIPE_PUBLISHABLE_KEY
    private val MY_STRIPE_SECRET_KEY = STRIPE_SECRET_KEY
    private val stripeUtils = StripeUtils(MY_STRIPE_PUBLISHABLE_KEY, MY_STRIPE_SECRET_KEY)
    private val gson = Gson()
    private var isChanged = false

     suspend fun addCardForNewUserOrExistingUser(
        activity: Activity,
        card: CardObject,
        email: String,
        stripeCustomerId: String,
    ): String {
        var strCustomerId = ""

        CoroutineScope(Dispatchers.IO).async {

            if (stripeCustomerId.equals("")) {
                GlobalScope.launch {
                    suspend {
                        strCustomerId = stripeUtils.createStripeCustomerWithUserEmail(
                            activity,
                            card,
                            email
                        )
                        Log.d("TAG", "onCreate: $strCustomerId")


                        withContext(Dispatchers.Main) {
                            if (strCustomerId.equals("")) {
                                activity.runOnUiThread(Runnable {
//                                    stripeUtils.hideProgressDialogStripe()
                                    stripeUtils.showAlertDialogForstripe(activity) {
                                        setTitle("STRIPE..!!")
                                        setMessage(stripeUtils.error)
                                        setCancelable(false)
                                        setPositiveButton("OK",
                                            DialogInterface.OnClickListener { dialogInterface, i ->
                                                dialogInterface.dismiss()
                                            })
                                    }
                                })
                            } else {
                                activity.runOnUiThread(Runnable {
                                    Toast.makeText(activity,
                                        "Card successfully added",
                                        Toast.LENGTH_SHORT).show()
                                })
                            }
                        }
                    }.invoke()
                }
            } else {
                GlobalScope.launch {
                    suspend {
                        strCustomerId =
                            stripeUtils.addNewCardToStripeCustomerWithCustomerID(
                                activity,
                                card,
                                stripeCustomerId
                            )
                                .toString()
                        Log.d("TAG", "onCreate: $strCustomerId")
                        withContext(Dispatchers.Main) {
                            if (strCustomerId.equals("")) {
                                activity.runOnUiThread(Runnable {
//                                    stripeUtils.hideProgressDialogStripe()
                                    stripeUtils.showAlertDialogForstripe(activity) {
                                        setTitle("STRIPE..!!")
                                        setMessage(stripeUtils.error)
                                        setCancelable(false)
                                        setPositiveButton("OK",
                                            DialogInterface.OnClickListener { dialogInterface, i ->
                                                dialogInterface.dismiss()
                                            })
                                    }
                                })
                            } else {
                                activity.runOnUiThread(Runnable {
                                    Toast.makeText(activity,
                                        "Card successfully added",
                                        Toast.LENGTH_SHORT).show()
                                })
                            }
                        }
                    }.invoke()
                }
            }
        }.await()
        return strCustomerId
    }

    fun getAddedCardList(activity: Activity,stripeCustomerId:String): List<StripeUtils.StripeCardData.Data> {
        activity.runOnUiThread(Runnable {
            stripeUtils.showProgressDialogStripe(activity)
        })
        var jsonData = stripeUtils.getAllStripeCard(stripeCustomerId)
        Log.e("---card List----", "getAllStripeCard: " + jsonData)
        var testModel =
            gson.fromJson(jsonData.toString(), StripeUtils.StripeCardData::class.java)
        val cardList = testModel.data
        activity.runOnUiThread(Runnable {
            stripeUtils.hideProgressDialogStripe()
        })
        return cardList
    }
    suspend fun makeDefaultCardAndGetList(activity: Activity, cardID:String, stripeCustomerId: String, checkCallBack: (Boolean) -> Unit){
        var isDefault=false

        CoroutineScope(Dispatchers.IO).async {
            activity.runOnUiThread(Runnable {
                stripeUtils.showProgressDialogStripe(activity)
            })
            isDefault = stripeUtils.updateDefaultcardWithCustomerId(
                activity, cardID,
                stripeCustomerId
            )
        }.await()
        activity.runOnUiThread(Runnable {
            stripeUtils.hideProgressDialogStripe()
        })
        checkCallBack(isDefault)
    }

    suspend fun deleteCard(activity: Activity, cardID:String, stripeCustomerId: String,checkCallBack: (Boolean) -> Unit){
        var isDefault=false

        CoroutineScope(Dispatchers.IO).async {
            activity.runOnUiThread(Runnable {
                stripeUtils.showProgressDialogStripe(activity)
            })
            isDefault = stripeUtils.deleteCard(
                activity,
                stripeCustomerId, cardID
            )
        }.await()
        activity.runOnUiThread(Runnable {
            stripeUtils.hideProgressDialogStripe()
        })
        checkCallBack(isDefault)
    }
}