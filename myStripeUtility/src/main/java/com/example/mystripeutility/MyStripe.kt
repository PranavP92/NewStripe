package com.example.mystripeutility

import StripeUtils
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.*

class MyStripe(private val STRIPE_PUBLISHABLE_KEY: String, private val STRIPE_SECRET_KEY: String) {

    val MY_STRIPE_PUBLISHABLE_KEY = STRIPE_PUBLISHABLE_KEY
    val MY_STRIPE_SECRET_KEY = STRIPE_SECRET_KEY
    val stripeUtils = StripeUtils(MY_STRIPE_PUBLISHABLE_KEY, MY_STRIPE_SECRET_KEY)
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
                                    stripeUtils.showAlertDialogForstripe(activity) {
                                        setTitle("ERROR..!!")
                                        setMessage(stripeUtils.error)
                                        setPositiveButton("OK",
                                            DialogInterface.OnClickListener { dialogInterface, i ->
                                                dialogInterface.dismiss()
                                            })
                                    }
                                })
                            } else {

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
                                    stripeUtils.showAlertDialogForstripe(activity) {
                                        setTitle("ERROR..!!")
                                        setMessage(stripeUtils.error)
                                        setPositiveButton("OK",
                                            DialogInterface.OnClickListener { dialogInterface, i ->
                                                dialogInterface.dismiss()
                                            })
                                    }
                                })
                            } else {

                            }
                        }
                    }.invoke()
                }
            }
        }.await()
        return strCustomerId
    }

    fun getAddedCardList(context: Context,stripeCustomerId:String): List<StripeUtils.StripeCardData.Data> {
        var jsonData = stripeUtils.getAllStripeCard(stripeCustomerId)
        Log.e("---card List----", "getAllStripeCard: " + jsonData)
        var testModel =
            gson.fromJson(jsonData.toString(), StripeUtils.StripeCardData::class.java)
        val cardList = testModel.data
        return cardList
    }
    suspend fun makeDefaultCardAndGetList(context: Context, cardID:String, stripeCustomerId: String):Boolean{
        var isDefault=false
        CoroutineScope(Dispatchers.IO).async {
            isDefault = stripeUtils.updateDefaultcardWithCustomerId(
                context, cardID,
                stripeCustomerId
            )
        }.await()
        return isDefault
    }

    suspend fun deleteCard(context: Context, cardID:String, stripeCustomerId: String):Boolean{
        var isDefault=false
        CoroutineScope(Dispatchers.IO).async {
            isDefault = stripeUtils.deleteCard(
                context,
                stripeCustomerId, cardID
            )
        }.await()
        return isDefault
    }
}