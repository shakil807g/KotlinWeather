package com.ardaozceviz.weather.view

import android.app.Activity
import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.ardaozceviz.weather.R
import com.ardaozceviz.weather.controller.LocalForecastData
import com.ardaozceviz.weather.controller.LocationServices
import com.ardaozceviz.weather.model.ForecastDataModel
import com.ardaozceviz.weather.model.ListItem
import com.ardaozceviz.weather.model.TAG_C_INTERFACE
import com.ardaozceviz.weather.view.adapter.ForecastListAdapter
import com.ardaozceviz.weather.view.mappers.ForecastDataMapper
import kotlinx.android.synthetic.main.activity_main.*


/**
 * Created by arda on 07/11/2017.
 */
class UserInterface(private val context: Context) {
    private var activity = context as Activity
    private val swipeRefreshLayout = activity.findViewById<SwipeRefreshLayout>(R.id.main_swipe_refresh_layout) as SwipeRefreshLayout

    // Snackbar
    //private val constraintLayout = activity.findViewById<ConstraintLayout>(R.id.main_constraint_layout) as ConstraintLayout
    private var retrySnackBar = Snackbar.make(swipeRefreshLayout, "Unable to retrieve weather data.", Snackbar.LENGTH_INDEFINITE)

    fun initialize() {
        Log.i(TAG_C_INTERFACE, "initialize() is executed.")
        swipeRefreshLayout.isRefreshing = true
        LocationServices(context).gpsPermission()
        /*
        Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
        performs a swipe-to-refresh gesture.
        */
        swipeRefreshLayout.setOnRefreshListener(
                {
                    Log.i(TAG_C_INTERFACE, "onRefresh called from swipeRefreshLayout")
                    // This method performs the actual data-refresh operation.
                    // The method calls setRefreshing(false) when it's finished.
                    LocationServices(context).gpsPermission()
                }
        )
    }

    fun updateUI(forecastDataModel: ForecastDataModel) {
        Log.d(TAG_C_INTERFACE, "updateUI() is executed.")
        val mappedForecastData = ForecastDataMapper(forecastDataModel)
        val mainViewImageResourceId = activity.resources.getIdentifier(mappedForecastData.iconName, "drawable", activity.packageName)

        // Today's information
        activity.main_view_city_name.text = mappedForecastData.location
        activity.main_view_date.text = mappedForecastData.currentDateTimeString
        activity.main_view_description.text = mappedForecastData.weatherDescription
        activity.main_view_temperature.text = mappedForecastData.temperature
        activity.main_view_wind.text = mappedForecastData.wind
        activity.main_view_image.setImageResource(mainViewImageResourceId)

        // Forecast recycler view information
        val forecastRecyclerView = activity.findViewById<RecyclerView>(R.id.main_view_forecast_recycler_view) as RecyclerView
        val adapter = ForecastListAdapter(context, mappedForecastData.listOfDaysForecastData)
        adapter.addOnclickListener{
           forecast: ListItem ->
            Log.d(TAG_C_INTERFACE,"$forecast")
        }

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        forecastRecyclerView.adapter = adapter
        forecastRecyclerView.layoutManager = layoutManager
        forecastRecyclerView.setHasFixedSize(true)
    }

    fun onError() {
        Log.d(TAG_C_INTERFACE, "onError() is executed.")
        stopRefresh()

        val localForecastData = LocalForecastData(context).retrieve()
        if (localForecastData == null) {
            Log.d(TAG_C_INTERFACE, "onError() localForecastData is null.")
            // No connection and no data info screen
            // ....
        } else {
            retrySnackBar = Snackbar.make(swipeRefreshLayout, "Unable to retrieve weather data.", Snackbar.LENGTH_LONG)
            if (!retrySnackBar.isShown) {
                retrySnackBar.setAction("Retry") { _ ->
                    Log.d(TAG_C_INTERFACE, "onError() Retry is clicked.")
                    swipeRefreshLayout.isRefreshing = true
                    LocationServices(context).gpsPermission()
                    retrySnackBar.dismiss()
                }
                retrySnackBar.setActionTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                retrySnackBar.show()
            }
        }
    }

    fun stopRefresh(forecastDataModel: ForecastDataModel? = null) {
        Log.d(TAG_C_INTERFACE, "stopRefresh() is executed.")
        swipeRefreshLayout.isEnabled = true
        swipeRefreshLayout.isRefreshing = false
        if (forecastDataModel != null) {
            Log.d(TAG_C_INTERFACE, "forecastDataModel is not null.")
            updateUI(forecastDataModel)
        }
    }
}