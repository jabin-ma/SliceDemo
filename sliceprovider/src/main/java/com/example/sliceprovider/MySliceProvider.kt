package com.example.sliceprovider

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.provider.Settings
import android.util.Log
import androidx.core.graphics.drawable.IconCompat
import androidx.slice.Slice
import androidx.slice.SliceProvider
import androidx.slice.builders.*
import androidx.slice.builders.ListBuilder.*
import java.util.*
import kotlin.concurrent.schedule

@SuppressLint("UnspecifiedImmutableFlag")
class MySliceProvider : SliceProvider() {

    companion object {
        var contentLoaded = false
        const val actualFare = "45 miles | 45 mins | $45.23"
        val delayContentSliceUri: Uri =
            Uri.parse("content://com.example.sliceprovider/delayContentSlice")
    }

    override fun onMapIntentToUri(intent: Intent?): Uri {
        Log.d("MySliceProvider", "onMapIntentToUri")
        return super.onMapIntentToUri(intent)
    }

    override fun onCreateSliceProvider(): Boolean {
        return true
    }

    override fun onBindSlice(sliceUri: Uri): Slice {
        return when (sliceUri.path) {
            "/basicRowSlice" -> createBasicRowSlice(sliceUri)
            "/basicHeaderSlice" -> createBasicHeaderSlice(sliceUri)
            "/basicActionClickSlice" -> createBasicInteractiveSlice(sliceUri)
            "/basicActionClickSliceWithKTX" -> createBasicInteractiveSliceWithKTX(sliceUri)
            "/rowSliceWithStartItem" -> createRowSliceWithStartItem(sliceUri)
            "/wifiToggleAction" -> createWifiToggleActionSlice(sliceUri)
            "/dynamicCountSlice" -> createDynamicSlice(sliceUri)
            "/inputRangeSlice" -> createInputRangeSlice(sliceUri)
            "/rangeSlice" -> createRangeSlice(sliceUri)
            "/headerSliceWithMoreActions" -> createHeaderSliceWithMoreActions(sliceUri)
            "/headerSliceWithHeaderAndRow" -> createSliceWithHeaderAndRow(sliceUri)
            "/gridRowSlice" -> createSliceWithGridRow(sliceUri)
            "/delayContentSlice" -> createSliceShowingLoading(sliceUri)
            "/seeMoreRowSlice" -> createSliceWithSeeMoreAction(sliceUri)
            "/combineSlices" -> createCombineSlices(sliceUri)
            "/trafficInfoSlice" -> createTrafficInfoSlice(sliceUri)
            else -> createErrorSlice(sliceUri)
        }
    }

    //region Basic Slices

    private fun createBasicRowSlice(sliceUri: Uri): Slice {
        return list(context!!, sliceUri, INFINITY) {
            row {
                title = "Welcome Android Slice"
                subtitle = "Row of Slice"
                // comment the following line to get the exception.
                primaryAction = createActivityAction(
                    Intent(context!!, MainActivity::class.java),
                    R.drawable.ic_pizza_slice_24,
                    ICON_IMAGE
                )
            }
        }
    }

    private fun createRowSliceWithStartItem(sliceUri: Uri): Slice {
        return list(context!!, sliceUri, INFINITY) {
            row {
                title = "Welcome Android Slice"
                subtitle = "It has Start Item"
                // comment the following line to get the exception.
                //it.primaryAction = createActivityAction(Intent(context!!, MainActivity::class.java), R.drawable.ic_pizza_slice_24, ICON_IMAGE)
                setTitleItem(
                    createActivityAction(
                        Intent(context!!, MainActivity::class.java),
                        R.drawable.ic_pizza_slice_24,
                        ICON_IMAGE
                    )
                )
            }
        }
    }

    private fun createRowSliceWithStartItem1(sliceUri: Uri): Slice {
        return list(context!!, sliceUri, INFINITY) {
            row {
                setTitleItem(
                    createActivityAction(
                        Intent(context!!, MainActivity::class.java),
                        R.drawable.ic_pizza_slice_24,
                        ICON_IMAGE
                    )
                )
                title = "Welcome Android Slice"
                subtitle = "It has Start Item"
            }
        }
    }

    private fun createBasicHeaderSlice(sliceUri: Uri): Slice {
        return list(context!!, sliceUri, INFINITY) {
            header {
                title = "Welcome Android Slice"
                subtitle = "Header of Slice"
            }
        }
    }

    private fun createBasicInteractiveSlice(sliceUri: Uri): Slice {
        return list(context!!, sliceUri, INFINITY) {
            row {
                title = "Android Slice"
                setSubtitle("Click Me !!", true)
                primaryAction = createPrimaryOpenMainActivityAction()
            }
        }
    }

    private fun createBasicInteractiveSliceWithKTX(sliceUri: Uri): Slice {
        return list(context!!, sliceUri, INFINITY) {
            row {
                title = "Android Slice w/ KTX builders"
                subtitle = "Click Me !!"
                primaryAction = createPrimaryOpenMainActivityAction()
            }
        }
    }

    //endregion

    //region Interactive Slice (eg. Wifi Toggle)
    private fun createWifiToggleActionSlice(sliceUri: Uri): Slice {
        val subTitle: String
        val wifiManager = context!!.getSystemService(Context.WIFI_SERVICE)
        val isWifiEnabled = wifiManager is WifiManager && wifiManager.isWifiEnabled
        subTitle = if (isWifiEnabled) {
            "Enabled"
        } else {
            "Not Enabled"
        }

        return list(context!!, sliceUri, INFINITY) {
            row {
                title = "Wifi"
                subtitle = subTitle
                primaryAction = createWiFiToggleAction(isWifiEnabled)
            }
        }
    }

    private fun createWiFiToggleAction(wifiEnabled: Boolean): SliceAction {
        val intent = Intent(
            context!!,
            MyBroadcastReceiver::class.java
        ).setAction(MyBroadcastReceiver.TOGGLE_WIFI).putExtra(
            MyBroadcastReceiver.EXTRA_VALUE_KEY, wifiEnabled
        )
        return SliceAction.createToggle(
            PendingIntent.getBroadcast(context!!, 0, intent, 0),
            "Toggle Wi-Fi",
            wifiEnabled
        )
    }
    //endregion

    //region Dynamic Slice

    private fun createDynamicSlice(sliceUri: Uri): Slice {
        val incrementPendingIntent = PendingIntent.getBroadcast(
            context!!,
            0,
            Intent(
                context!!,
                MyBroadcastReceiver::class.java
            ).setAction(MyBroadcastReceiver.INCREMENT_COUNTER_ACTION).putExtra(
                MyBroadcastReceiver.EXTRA_VALUE_KEY,
                MyBroadcastReceiver.currentValue + 1
            ), PendingIntent.FLAG_UPDATE_CURRENT
        )

        val decrementPendingIntent = PendingIntent.getBroadcast(
            context!!,
            0,
            Intent(
                context!!,
                MyBroadcastReceiver::class.java
            ).setAction(MyBroadcastReceiver.DECREMENT_COUNTER_ACTION).putExtra(
                MyBroadcastReceiver.EXTRA_VALUE_KEY,
                MyBroadcastReceiver.currentValue - 1
            ), PendingIntent.FLAG_UPDATE_CURRENT
        )

        val incrementAction = SliceAction.create(
            incrementPendingIntent,
            IconCompat.createWithResource(context!!, R.drawable.ic_plus),
            ICON_IMAGE,
            "Increment Counter."
        )
        val decrementAction = SliceAction.create(
            decrementPendingIntent,
            IconCompat.createWithResource(context!!, R.drawable.ic_minus),
            ICON_IMAGE,
            "Decrement Counter."
        )

        return list(context!!, sliceUri, INFINITY) {
            row {
                primaryAction = createPrimaryOpenMainActivityAction()
                title = "Total Count : ${MyBroadcastReceiver.currentValue}"
                subtitle = "This is dynamic slice demo"
                addEndItem(incrementAction)
                addEndItem(decrementAction)
            }
        }
    }

    //endregion

    //region Delayed Content Slice example
    private fun createSliceShowingLoading(sliceUri: Uri): Slice {
        // We’re waiting to load the time to work so indicate that on the slice by
        // setting the subtitle with the overloaded method and indicate true.
        Timer("SettingUp", false).schedule(2000) { loadSliceContents() }
        return list(context!!, sliceUri, INFINITY) {
            row {
                title = "Ride to work"
                if (contentLoaded) {
                    setSubtitle(actualFare, false)
                } else {
                    setSubtitle(null, true)
                }
                primaryAction = createPrimaryOpenMainActivityAction()
            }
        }
    }

    private fun loadSliceContents() {
        contentLoaded = true
        context!!.contentResolver.notifyChange(delayContentSliceUri, null)
    }
    //endregion

    //region Range/InputRange Slices
    private fun createRangeSlice(sliceUri: Uri): Slice {
        return list(context!!, sliceUri, INFINITY) {
            range {
                title = "Current brightness level"
                subtitle = "25 %"
                max = 100
                value = 25
                primaryAction = createPrimaryOpenMainActivityAction()
            }
        }
    }

    private fun createInputRangeSlice(sliceUri: Uri): Slice {
        val toggleAction = createBrightnessAction()

        return list(context!!, sliceUri, INFINITY) {
            inputRange {
                title = "Adaptive brightness"
                subtitle = "Optimizes brightness for available light"
                min = 0
                max = 100
                value = 45
                inputAction = createSettingsPendingIntent()
                // not working primary action.
                primaryAction = createPrimaryOpenMainActivityAction()
            }
        }
    }
    //endregion

    //region Header/Row Builder Examples
    private fun createHeaderSliceWithMoreActions(sliceUri: Uri): Slice {
        return list(context!!, sliceUri, INFINITY) {
            header {
                title = "Header with 2 actions"
                subtitle = "Choose any action"
                summary = "Choose any action from two actions"
            }
            addAction(
                createActivityAction(
                    Intent(Settings.ACTION_WIFI_SETTINGS),
                    R.drawable.ic_wifi_24,
                    ICON_IMAGE
                )
            )
            addAction(
                createActivityAction(
                    Intent(Settings.ACTION_BLUETOOTH_SETTINGS),
                    R.drawable.ic_bluetooth_24,
                    ICON_IMAGE
                )
            )
        }
    }

    private fun createSliceWithHeaderAndRow(sliceUri: Uri): Slice {
        return list(context!!, sliceUri, INFINITY) {
            header {
                title = "Get a ride."
                subtitle = "Ride in 4 min."
                summary = "Work in 45 min | Home in 15 min."
            }
            row {
                title = "Home"
                subtitle = "15 miles | 15 min | $15.23"
                primaryAction = createActivityAction(
                    Intent(context!!, MainActivity::class.java),
                    R.drawable.ic_work_24,
                    ICON_IMAGE
                )
            }
            row {
                title = "Work"
                subtitle = "45 miles | 45 min | $15.23"
                addEndItem(
                    createActivityAction(
                        Intent(context!!, MainActivity::class.java),
                        R.drawable.ic_work_24,
                        ICON_IMAGE
                    )
                )
            }
            row {
                title = "Slice Row"
                subtitle = "contains start and end items"
                primaryAction = createActivityAction(
                    Intent(context!!, MainActivity::class.java),
                    R.drawable.ic_work_24,
                    ICON_IMAGE
                )
                setTitleItem(
                    createActivityAction(
                        Intent(context!!, MainActivity::class.java),
                        R.drawable.ic_pizza_slice_24,
                        ICON_IMAGE
                    )
                )
            }
            setAccentColor(context!!.getColor(R.color.colorAccent))
        }
    }
    //endregion

    //region GridRowBuilder examples
    private fun createSliceWithGridRow(sliceUri: Uri): Slice {

        return list(context!!, sliceUri, INFINITY) {
            header {
                title = "Famous restaurants"
                primaryAction = createActivityAction(
                    Intent(context!!, MainActivity::class.java),
                    R.drawable.ic_restaurant_24,
                    ICON_IMAGE
                )
            }
            gridRow {
                cell {
                    addImage(
                        IconCompat.createWithResource(context!!, R.drawable.restaurant1),
                        LARGE_IMAGE
                    )
                    addTitleText("Top Restaurant")
                    addText("0.3 mil")
                    contentIntent = createSettingsPendingIntent()
                }
                cell {
                    addImage(
                        IconCompat.createWithResource(context!!, R.drawable.restaurant2),
                        LARGE_IMAGE
                    )
                    addTitleText("Fast and Casual")
                    addText("0.5 mil")
                    contentIntent = createSettingsPendingIntent()
                }
                cell {
                    addImage(
                        IconCompat.createWithResource(context!!, R.drawable.restaurant3),
                        LARGE_IMAGE
                    )
                    addTitleText("Casual Diner")
                    addText("0.9 mi")
                    contentIntent = createSettingsPendingIntent()
                }
                cell {
                    addImage(
                        IconCompat.createWithResource(context!!, R.drawable.restaurant4),
                        LARGE_IMAGE
                    )
                    addTitleText("Ramen Spot")
                    addText("1.2 mi")
                    contentIntent = createSettingsPendingIntent()
                }
                setSeeMoreAction(createSettingsPendingIntent())
                // As per the google doc if whole grid row clicks then it should trigger the primary action. But it's not working.
                //developer.android.com/reference/androidx/slice/builders/GridRowBuilder#setPrimaryAction(androidx.slice.builders.SliceAction)
                primaryAction = createPrimaryOpenMainActivityAction()
                // uncomment the below line to use the custom see more cell.
                // seeMoreCell = GridRowBuilder.CellBuilder().addTitleText("Custom Title").addText("Custom Text").addImage(IconCompat.createWithResource(context!!!!, R.drawable.ic_android_24), SMALL_IMAGE).setContentIntent(createSettingsPendingIntent())
            }
        }
    }
    //endregion

    //region seeMoreAction/Row example, This isn't working, need to check why
    private fun createSliceWithSeeMoreAction(sliceUri: Uri): Slice {

        val gmmIntentUri = Uri.parse("geo:37.7749,-122.4194?q=restaurants")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")


        return list(context!!, sliceUri, INFINITY) {
            header {
                title = "Near by restaurants"
                primaryAction = createPrimaryOpenMainActivityAction()
            }
            row {
                primaryAction = createActivityAction(mapIntent, R.drawable.ic_place_24, ICON_IMAGE)
                title = "Bakora, Mediterranean food"
                setTitleItem(
                    IconCompat.createWithResource(context!!, R.drawable.restaurant1),
                    SMALL_IMAGE
                )
            }
            row {
                primaryAction = createActivityAction(mapIntent, R.drawable.ic_place_24, ICON_IMAGE)
                title = "Taj, Indian food"
                setTitleItem(
                    IconCompat.createWithResource(context!!, R.drawable.restaurant2),
                    SMALL_IMAGE
                )
            }
            row {
                primaryAction = createActivityAction(mapIntent, R.drawable.ic_place_24, ICON_IMAGE)
                title = "Primer Pizza, Italian food"
                setTitleItem(
                    IconCompat.createWithResource(context!!, R.drawable.restaurant3),
                    SMALL_IMAGE
                )
            }
            setSeeMoreAction(
                PendingIntent.getActivity(
                    context!!,
                    0,
                    Intent(context!!, MainActivity::class.java),
                    0
                )
            )
        }
    }
    //endregion

    //region Combine Row/Item tempaltes
    private fun createCombineSlices(sliceUri: Uri): Slice {

        return list(context!!, sliceUri, INFINITY) {
            row {
                title = "Upcoming Trip: Seattle"
                subtitle = "Aug 15-20 • 5 Guests"
                primaryAction = createActivityAction(
                    Intent(context!!, MainActivity::class.java),
                    R.drawable.ic_email_24,
                    ICON_IMAGE
                )
            }
            gridRow {
                cell {
                    addImage(
                        IconCompat.createWithResource(context!!, R.drawable.restaurant1),
                        LARGE_IMAGE
                    )
                }
            }
            gridRow {
                cell {
                    addTitleText("Check In")
                    addText("2:00 PM, Aug 15")
                }
                cell {
                    addTitleText("Check In")
                    addText("11:00 AM, Aug 20")
                }
            }
        }
    }

    private fun createTrafficInfoSlice(sliceUri: Uri): Slice {

        val gmmIntentUri = Uri.parse("geo:37.7749,-122.4194")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        return list(context!!, sliceUri, INFINITY) {
            header {
                title = "Heavy traffic in your area"
                subtitle = "Typical conditions delays up to 28"
            }
            gridRow {
                cell {
                    addImage(
                        IconCompat.createWithResource(context!!, R.drawable.ic_home_24),
                        ICON_IMAGE
                    )
                    addTitleText("Home")
                    addText("30 min")
                    contentIntent = createMapsActivityPendingIntent()
                }
                cell {
                    addImage(
                        IconCompat.createWithResource(context!!, R.drawable.ic_work_24),
                        ICON_IMAGE
                    )
                    addTitleText("Work")
                    addText("45 min")
                    contentIntent = createMapsActivityPendingIntent()
                }
                cell {
                    addImage(
                        IconCompat.createWithResource(context!!, R.drawable.ic_restaurant_24),
                        ICON_IMAGE
                    )
                    addTitleText("Restaurant")
                    addText("5 min")
                    contentIntent = createMapsActivityPendingIntent()
                }
                // As per the google doc if whole grid row clicks then it should trigger the primary action. But it's not working.
                //developer.android.com/reference/androidx/slice/builders/GridRowBuilder#setPrimaryAction(androidx.slice.builders.SliceAction)
                primaryAction =
                    createActivityAction(mapIntent, R.drawable.ic_directions_24, ICON_IMAGE)
            }
        }
    }
    //endregion

    //region Helper methods
    private fun createSettingsPendingIntent(): PendingIntent {
        return PendingIntent.getActivity(
            context!!,
            0,
            Intent(Settings.ACTION_SETTINGS),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createMapsActivityPendingIntent(): PendingIntent {
        val gmmIntentUri = Uri.parse("geo:37.7749,-122.4194")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        return PendingIntent.getActivity(
            context!!,
            0,
            mapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createPrimaryOpenMainActivityAction(): SliceAction {
        val intent = Intent(context!!, MainActivity::class.java)
        return SliceAction.create(
            PendingIntent.getActivity(context!!, 0, intent, 0),
            IconCompat.createWithResource(context!!, R.drawable.ic_open_24),
            ICON_IMAGE,
            "Open MainActivity."
        )
    }

    private fun createBrightnessAction(): SliceAction {
        val intent = Intent(context!!, MyBroadcastReceiver::class.java)
        return SliceAction.create(
            PendingIntent.getBroadcast(context!!, 0, intent, 0),
            IconCompat.createWithResource(context!!, R.drawable.ic_brightness_auto_24),
            ICON_IMAGE,
            "Toggle adaptive brightness"
        )
    }

    private fun createActivityAction(
        actionIntent: Intent,
        drawableInt: Int,
        imageMode: Int
    ): SliceAction {
        return SliceAction.create(
            PendingIntent.getActivity(context!!, 0, actionIntent, 0),
            IconCompat.createWithResource(context!!, drawableInt),
            imageMode,
            "Open MainActivity."
        )
    }

    private fun createErrorSlice(sliceUri: Uri): Slice {
        return list(context!!, sliceUri, INFINITY) {
            row {
                title = "URI not found, Error."
                primaryAction = createPrimaryOpenMainActivityAction()
            }
        }
    }
    //endregion
}