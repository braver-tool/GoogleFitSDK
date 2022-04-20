package com.braver.googlefit

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessActivities
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType.*
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.data.HealthDataTypes
import com.google.android.gms.fitness.data.HealthFields
import com.google.android.gms.fitness.data.Session
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.SessionReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.google.android.gms.fitness.result.SessionReadResponse
import java.lang.String.format
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class MainViewModel : ViewModel() {
    companion object {
        const val DOUBLE_SLASH_DOT = "\\."
        const val WORK_DURATION = 1500L
        const val IS_GOOGLE_AUTH_SIGNED: String = "is_google_auth_signed"
        const val USER_NAME: String = "user_name"
        const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE: Int = 2252

        // Vitals
        const val G_FIT_BLOOD_PRESSURE_DATA_TYPE = "com.google.blood_pressure"
        const val G_FIT_BLOOD_GLUCOSE_DATA_TYPE = "com.google.blood_glucose"
        const val G_FIT_OXYGEN_SATURATION_DATA_TYPE = "com.google.oxygen_saturation"
        const val G_FIT_TEMPERATURE_DATA_TYPE = "com.google.body.temperature"
        const val G_FIT_SCALE_DATA_TYPE = "com.google.weight"
        const val G_FIT_HEART_RATE_DATA_TYPE = "com.google.heart_rate.bpm"

        const val BLOOD_PRESSURE: String = "Blood Pressure"
        const val BLOOD_GLUCOSE: String = "Blood Glucose"
        const val O2_SATURATION: String = "O2 Saturation"
        const val BODY_TEMPERATURE: String = "Body Temperature"
        const val BODY_WEIGHT: String = "Body Weight"
        const val HEART_RATE: String = "Heart Rate"

        const val SYS: String = "SYS"
        const val DIA: String = "DIA"
        const val H_RATE: String = "HR"
        const val SPO2: String = "SPO2"
        const val WEIGHT: String = "Weight"
        const val TEMP: String = "Temp"
        const val GLUCOSE: String = "Glucose"

        const val BLOOD_PRESSURE_MEASURE: String = "mmHg"
        const val BLOOD_GLUCOSE_MEASURE: String = "mg/dL"
        const val O2_SATURATION_MEASURE: String = "%"
        const val TEMPERATURE_MEASURE: String = "°F"
        const val WEIGHT_MEASURE: String = "lbs"
        const val HEART_RATE_MEASURE: String = "bpm"

        //Activity
        const val G_FIT_ACTIVITY_FIELD_CALORIES = "calories"
        const val G_FIT_ACTIVITY_FIELD_DURATION = "duration"
        const val G_FIT_ACTIVITY_FIELD_DISTANCE = "distance"
        const val G_FIT_ACTIVITY_FIELD_STEPS = "steps"

        //Sleep
        const val SLEEP: String = "Sleep"
        val G_FIT_SLEEP_STAGES = arrayOf(
            "Unused",
            "Awake (during sleep)",
            "Sleep",
            "Out-of-bed",
            "Light sleep",
            "Deep sleep",
            "REM sleep"
        )

        /**
         * Returns the activity name from the activity ID
         */
        fun getActivityName(id: Int): String {
            return getActivityNameForGoogleFit[id]
        }

        /**
         * This method used to return the e name of activity usinf activity ID
         */
        private val getActivityNameForGoogleFit = arrayOf( /*0 to 10*/
            "",
            "Biking",
            "",
            "",
            "Unknown",
            "Tilting",
            "",
            "Walking",
            "Running",
            "Aerobics",
            "Badminton",  /*11 to 20*/
            "Baseball",
            "Basketball",
            "Biathlon",
            "Handbiking",
            "Mountain biking",
            "Road biking",
            "Spinning",
            "Stationary biking",
            "Utility biking",
            "Boxing",  /*21 to 30*/
            "Calisthenics",
            "Circuit training",
            "Cricket",
            "Dancing",
            "Elliptical",
            "Fencing",
            "American Football",
            "Australian Football",
            "Football",
            "Frisbee",  /*31 to 40*/
            "Gardening",
            "Golf",
            "Gymnastics",
            "Handball",
            "Hiking",
            "Hockey",
            "Horseback riding",
            "Housework",
            "Jumping rope",
            "Kayaking",  /*41 to 50*/
            "Kettlebell",
            "Kickboxing",
            "Kitesurfing",
            "Martial arts",
            "Meditation",
            "Mixed martial arts",
            "P90X",
            "Paragliding",
            "Pilates",
            "Polo",  /*51 to 60*/
            "Racquetball",
            "Rock climbing",
            "Rowing",
            "Rowing machine",
            "Rugby",
            "Jogging",
            "Sand Running",
            "Running",
            "Sailing",
            "Scuba diving",  /*61 to 70*/
            "Skateboarding",
            "Skating",
            "Cross skating",
            "Inline skating",
            "Skiing",
            "Backcountry skiing",
            "Cross country skiing",
            "Downhillskiing",
            "Kite skiing",
            "Roller skiing",  /*71 to 80*/
            "Sledding",
            "",
            "Snowboarding",
            "Snowmobile",
            "Snowshoeing",
            "Squash",
            "Stair climbing",
            "Stair-climbing machine",
            "Stand-up paddleboarding",
            "Strength training",  /*81 to 90*/
            "Surfing",
            "Swimming",
            "Pool Swimming",
            "Open Water Swimming",
            "Table tennis",
            "Team sports",
            "Tennis",
            "Treadmill",
            "Volleyball",
            "Beach Volleyball",  /*91 to 100*/
            "Indoor Volleyball",
            "Wakeboarding",
            "Fitness Walking",
            "Nording walking",
            "Treadmill Walking",
            "Water polo",
            "Weightlifting",
            "Wheelchair",
            "Wind surfing",
            "Yoga",  /*101 to 110*/
            "Zumba",
            "Diving",
            "Ergometer",
            "Ice skating",
            "Indoor skating",
            "Curling",
            "",
            "Other",
            "",
            "",  /*111 to 120*/
            "",
            "",
            "Crossfit",
            "High Intensity Interval Training",
            "Interval Training",
            "Stroller Walking",
            "Elevator",
            "Escalator",
            "Archery",
            "Softball",  /*121 to 122*/
            "",
            "Guided Breathing" /*total 123*/
        )
    }

    private var googleFitItemsList: MutableList<GoogleFitDataModel> =
        ArrayList<GoogleFitDataModel>()
    private val mutableLiveData: MutableLiveData<List<GoogleFitDataModel>> = MutableLiveData()

    private val initTime = SystemClock.uptimeMillis()
    fun isDataReady() = SystemClock.uptimeMillis() - initTime > WORK_DURATION
    fun convertToDate(givenDate: String): Long {
        if (givenDate.isEmpty()) {
            return Calendar.getInstance().timeInMillis
        }
        val format: DateFormat =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            val date = format.parse(givenDate)
            val calendar = Calendar.getInstance()
            if (date != null) {
                calendar.time = date
            }
            calendar.timeInMillis
        } catch (e: ParseException) {
            Calendar.getInstance().timeInMillis
        }
    }

    fun getFitnessSignInOptions(): FitnessOptions {
        // Request access to step count data from Fit history
        return FitnessOptions.builder()
            .addDataType(HealthDataTypes.TYPE_BLOOD_GLUCOSE, FitnessOptions.ACCESS_READ)
            .addDataType(TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .addDataType(TYPE_WEIGHT, FitnessOptions.ACCESS_READ)
            .addDataType(HealthDataTypes.TYPE_BODY_TEMPERATURE, FitnessOptions.ACCESS_READ)
            .addDataType(HealthDataTypes.TYPE_BLOOD_PRESSURE, FitnessOptions.ACCESS_READ)
            .addDataType(HealthDataTypes.TYPE_OXYGEN_SATURATION, FitnessOptions.ACCESS_READ)
            .addDataType(TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
            .addDataType(TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
            .addDataType(TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .addDataType(TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
            .build()
    }

    fun isGoogleFitPermissionGranted(context: Context?): Boolean {
        val fitnessOptions: FitnessOptions =
            getFitnessSignInOptions()
        return GoogleSignIn.hasPermissions(
            GoogleSignIn.getLastSignedInAccount(context!!),
            fitnessOptions
        )
    }

    fun getGoogleLastSignedInAccount(mActivity: Activity): GoogleSignInAccount {
        return GoogleSignIn.getLastSignedInAccount(mActivity)!!
    }

    fun getGoogleSignInClient(context: Activity): GoogleSignInClient {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        return GoogleSignIn.getClient(context, gso)
    }

    /**
     * This method is used to convert date for notification time
     */
    private fun getNotificationTimeMills(notificationTimeDate: String): Long {
        var timeMills = Calendar.getInstance().timeInMillis
        try {
            val format: DateFormat =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date1 = format.parse(notificationTimeDate)
            val alarmTime = Calendar.getInstance()
            if (date1 != null) {
                alarmTime.time = date1
            }
            timeMills = alarmTime.timeInMillis
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return timeMills
    }

    private fun getDifferenceTimeForDuration(startTime: String, endTime: String): String {
        val dateStart = "$startTime:00"
        val dateEnd = "$endTime:00"
        //val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        var value = ""
        val d1: Date
        val d2: Date
        try {
            d1 = formatter.parse(dateStart)!!
            d2 = formatter.parse(dateEnd)!!
            //in milliseconds
            val diff = d2.time - d1.time
            value = (diff / 60000).toString() // MilliSeconds to Minutes
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return value
    }

    /**
     * This method is used to convert date long into string format
     */
    private fun getDateFromLongForGoogleFit(millisecond: Long): String {
        try {
            //val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = millisecond
            return formatter.format(calendar.time)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun getWholeValue(value: String): String {
        val roundValue: String = try {
            if (value.contains(".")) if (value.split(DOUBLE_SLASH_DOT).toTypedArray()[1]
                    .substring(0, 1).toInt() < 5
            ) value.split(DOUBLE_SLASH_DOT).toTypedArray()[0] else (value.split(DOUBLE_SLASH_DOT)
                .toTypedArray()[0]
                .toInt() + 1).toString() else value
        } catch (e: java.lang.Exception) {
            value
        }
        return roundValue
    }

    // Vitals
    private fun queryFitnessDataRequestForVitals(
        vitalsStartDate: String,
        vitalsEndDate: String
    ): DataReadRequest? {
        return try {
            val vitalsStartTime: Long = getNotificationTimeMills(vitalsStartDate)
            val vitalsEndTime: Long = getNotificationTimeMills(vitalsEndDate)
            Log.i("##BraverGFit", "----->vitalsStartDate---->$vitalsStartDate")
            Log.i("##BraverGFit", "----->vitalsEndDate---->$vitalsEndDate")
            DataReadRequest.Builder()
                .read(HealthDataTypes.TYPE_BLOOD_PRESSURE)
                .read(TYPE_HEART_RATE_BPM)
                .read(HealthDataTypes.TYPE_OXYGEN_SATURATION)
                .read(HealthDataTypes.TYPE_BLOOD_GLUCOSE)
                .read(HealthDataTypes.TYPE_BODY_TEMPERATURE)
                .read(TYPE_WEIGHT)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(vitalsStartTime, vitalsEndTime, TimeUnit.MILLISECONDS)
                .build()
        } catch (e: Exception) {
            Log.i("##BraverGFit", "----->queryFitnessDataRequestForVitals---->" + e.message)
            null
        }
    }

    fun getVitalsData(
        mActivity: Activity,
        startDate: String,
        endDate: String
    ): MutableLiveData<List<GoogleFitDataModel>> {
        Fitness.getHistoryClient(mActivity, getGoogleLastSignedInAccount(mActivity))
            .readData(queryFitnessDataRequestForVitals(startDate, endDate)!!)
            .addOnSuccessListener { dataReadResult: DataReadResponse? ->
                if (dataReadResult != null && dataReadResult.buckets.size > 0) {
                    googleFitItemsList.clear()
                    try {
                        for (i in dataReadResult.buckets.indices) {
                            val dataSets = dataReadResult.buckets[i].dataSets
                            for (j in dataSets.indices) {
                                if (dataSets[j].dataPoints.size > 0) {
                                    when (dataSets[j].dataType.name) {
                                        G_FIT_BLOOD_PRESSURE_DATA_TYPE -> {
                                            for (dp in dataSets[j].dataPoints) {
                                                val addedDate: String =
                                                    getDateFromLongForGoogleFit(
                                                        dp.getStartTime(
                                                            TimeUnit.MILLISECONDS
                                                        )
                                                    )
                                                val sysFloatValue =
                                                    dp.getValue(HealthFields.FIELD_BLOOD_PRESSURE_SYSTOLIC)
                                                        .asFloat()
                                                val diaFloatValue =
                                                    dp.getValue(HealthFields.FIELD_BLOOD_PRESSURE_DIASTOLIC)
                                                        .asFloat()
                                                val systolic: String =
                                                    sysFloatValue.roundToInt().toString()
                                                val diastolic: String =
                                                    diaFloatValue.roundToInt().toString()
                                                googleFitItemsList.add(
                                                    GoogleFitDataModel(
                                                        BLOOD_PRESSURE, addedDate,
                                                        SYS,
                                                        DIA, "", "", systolic, diastolic, "", "",
                                                        BLOOD_PRESSURE_MEASURE,
                                                        BLOOD_PRESSURE_MEASURE, "", ""
                                                    )
                                                )
                                            }
                                        }
                                        G_FIT_HEART_RATE_DATA_TYPE -> {
                                            for (dp in dataSets[j].dataPoints) {
                                                val addedDate: String =
                                                    getDateFromLongForGoogleFit(
                                                        dp.getStartTime(
                                                            TimeUnit.MILLISECONDS
                                                        )
                                                    )
                                                val heartRateFloatValue =
                                                    dp.getValue(dp.dataType.fields[0]).asFloat()
                                                val heartRate: String =
                                                    heartRateFloatValue.roundToInt().toString()
                                                googleFitItemsList.add(
                                                    GoogleFitDataModel(
                                                        HEART_RATE, addedDate,
                                                        H_RATE,
                                                        "", "", "", heartRate, "", "", "",
                                                        HEART_RATE_MEASURE,
                                                        "", "", ""
                                                    )
                                                )
                                            }
                                        }
                                        G_FIT_OXYGEN_SATURATION_DATA_TYPE -> {
                                            for (dp in dataSets[j].dataPoints) {
                                                val addedDate: String =
                                                    getDateFromLongForGoogleFit(
                                                        dp.getStartTime(
                                                            TimeUnit.MILLISECONDS
                                                        )
                                                    )
                                                //spo2
                                                val spo2FloatValue =
                                                    dp.getValue(HealthFields.FIELD_OXYGEN_SATURATION)
                                                        .asFloat()
                                                val spo2Value: String =
                                                    spo2FloatValue.roundToInt().toString()
                                                googleFitItemsList.add(
                                                    GoogleFitDataModel(
                                                        O2_SATURATION, addedDate,
                                                        SPO2,
                                                        "", "", "", spo2Value, "", "", "",
                                                        O2_SATURATION_MEASURE,
                                                        "", "", ""
                                                    )
                                                )
                                            }
                                        }
                                        G_FIT_SCALE_DATA_TYPE -> {
                                            for (dp in dataSets[j].dataPoints) {
                                                val addedDate: String =
                                                    getDateFromLongForGoogleFit(
                                                        dp.getStartTime(
                                                            TimeUnit.MILLISECONDS
                                                        )
                                                    )
                                                val scale =
                                                    dp.getValue(dp.dataType.fields[0]).asFloat()
                                                val scaleAsLbs = scale * 2.20462262 // KG to lbs

                                                val actualScale = String.format(
                                                    Locale.getDefault(),
                                                    "%.1f",
                                                    scaleAsLbs
                                                )
                                                googleFitItemsList.add(
                                                    GoogleFitDataModel(
                                                        BODY_WEIGHT, addedDate,
                                                        WEIGHT,
                                                        "", "", "", actualScale, "", "", "",
                                                        WEIGHT_MEASURE,
                                                        "", "", ""
                                                    )
                                                )
                                            }
                                        }
                                        G_FIT_TEMPERATURE_DATA_TYPE -> {
                                            for (dp in dataSets[j].dataPoints) {
                                                val addedDate: String =
                                                    getDateFromLongForGoogleFit(
                                                        dp.getStartTime(
                                                            TimeUnit.MILLISECONDS
                                                        )
                                                    )
                                                val temperature =
                                                    dp.getValue(HealthFields.FIELD_BODY_TEMPERATURE)
                                                        .asFloat()
                                                val temperatureAsFahrenheit =
                                                    (temperature * 9 / 5 + 32).toDouble() //celsius to fahrenheit
                                                val actualTemp = String.format(
                                                    Locale.getDefault(),
                                                    "%.1f",
                                                    temperatureAsFahrenheit
                                                )
                                                googleFitItemsList.add(
                                                    GoogleFitDataModel(
                                                        BODY_TEMPERATURE, addedDate,
                                                        TEMP,
                                                        "", "", "", actualTemp, "", "", "",
                                                        TEMPERATURE_MEASURE,
                                                        "", "", ""
                                                    )
                                                )
                                            }
                                        }
                                        G_FIT_BLOOD_GLUCOSE_DATA_TYPE -> {
                                            for (dp in dataSets[j].dataPoints) {
                                                val addedDate: String =
                                                    getDateFromLongForGoogleFit(
                                                        dp.getStartTime(
                                                            TimeUnit.MILLISECONDS
                                                        )
                                                    )
                                                val glucoseFloatValue =
                                                    dp.getValue(HealthFields.FIELD_BLOOD_GLUCOSE_LEVEL)
                                                        .asFloat()
                                                val glucoseValue: String =
                                                    glucoseFloatValue.roundToInt().toString()
                                                googleFitItemsList.add(
                                                    GoogleFitDataModel(
                                                        BLOOD_GLUCOSE, addedDate,
                                                        GLUCOSE,
                                                        "", "", "", glucoseValue, "", "", "",
                                                        BLOOD_GLUCOSE_MEASURE,
                                                        "", "", ""
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        mutableLiveData.setValue(googleFitItemsList)
                    } catch (e: java.lang.Exception) {
                        Log.d(
                            "##BraverGFit",
                            "----->parseVitalsDataResponseFromGFit------>runOnUiThread---->" + e.message
                        )
                    }
                } else {
                    mutableLiveData.setValue(googleFitItemsList)
                }

            }
            .addOnFailureListener { e: java.lang.Exception ->
                Log.i(
                    "##BraverGFit",
                    "----->getHistoryClient-->getVitalsData----->OnFailureListener---->" + e.message
                )
                mutableLiveData.setValue(googleFitItemsList)
            }
        return mutableLiveData
    }

    // Activity
    private fun queryFitnessDataRequestForActivity(
        vitalsStartDate: String,
        vitalsEndDate: String
    ): DataReadRequest? {
        return try {
            val activityStartTime: Long = getNotificationTimeMills(vitalsStartDate)
            val activityEndTime: Long = getNotificationTimeMills(vitalsEndDate)
            Log.i("##BraverGFit", "----->activityStartTime---->$vitalsStartDate")
            Log.i("##BraverGFit", "----->activityEndTime---->$vitalsEndDate")
            DataReadRequest.Builder()
                .read(TYPE_DISTANCE_DELTA)
                .read(TYPE_CALORIES_EXPENDED)
                .read(TYPE_STEP_COUNT_DELTA)
                .read(TYPE_MOVE_MINUTES)
                .bucketByActivitySegment(1, TimeUnit.MINUTES)
                .setTimeRange(activityStartTime, activityEndTime, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build()
        } catch (e: Exception) {
            Log.i("##BraverGFit", "----->queryFitnessDataRequestForVitals---->" + e.message)
            null
        }
    }

    fun getActivityData(
        mActivity: Activity,
        startDate: String,
        endDate: String
    ): MutableLiveData<List<GoogleFitDataModel>> {
        Fitness.getHistoryClient(mActivity, getGoogleLastSignedInAccount(mActivity))
            .readData(queryFitnessDataRequestForActivity(startDate, endDate)!!)
            .addOnSuccessListener { dataReadResult: DataReadResponse? ->
                if (dataReadResult != null && dataReadResult.buckets.size > 0) {
                    googleFitItemsList.clear()
                    try {
                        for (i in dataReadResult.buckets.indices) {
                            try {
                                val googleFitDataModel = GoogleFitDataModel()
                                val bucket = dataReadResult.buckets[i]
                                val activityID = bucket.zzd()
                                val activityNameFromArray: String =
                                    getActivityName(activityID)
                                if (activityNameFromArray.isNotEmpty() && activityNameFromArray != "Unknown") {
                                    val activityStartDateTime: String =
                                        getDateFromLongForGoogleFit(
                                            bucket.getStartTime(
                                                TimeUnit.MILLISECONDS
                                            )
                                        )
                                    val activityEndDateTime: String =
                                        getDateFromLongForGoogleFit(
                                            bucket.getEndTime(
                                                TimeUnit.MILLISECONDS
                                            )
                                        )
                                    googleFitDataModel.moduleName = activityNameFromArray
                                    googleFitDataModel.createdDate =
                                        activityStartDateTime.plus(" - ").plus(activityEndDateTime)
                                    val dataSets = bucket.dataSets
                                    var calories: Int
                                    var stepCheck = 0f
                                    var distanceCheck = 0f
                                    for (j in dataSets.indices) {
                                        for (dataPoint in dataSets[j].dataPoints) {
                                            try {
                                                for (field in dataPoint.dataType.fields) {
                                                    when (field.name) {
                                                        G_FIT_ACTIVITY_FIELD_CALORIES -> {
                                                            calories =
                                                                dataPoint.getValue(Field.FIELD_CALORIES)
                                                                    .asFloat()
                                                                    .toInt()
                                                            googleFitDataModel.dataTypeTwo =
                                                                "Calories"
                                                            googleFitDataModel.dataValueTwo =
                                                                calories.toString()
                                                            googleFitDataModel.dataTwoMeasure =
                                                                "kcal"
                                                        }
                                                        G_FIT_ACTIVITY_FIELD_DURATION -> {
                                                            //duration = dataPoint.getValue(Field.FIELD_DURATION).asInt();
                                                            val durationString: String =
                                                                getDifferenceTimeForDuration(
                                                                    activityStartDateTime,
                                                                    activityEndDateTime
                                                                )
                                                            googleFitDataModel.dataTypeFour =
                                                                "Duration"
                                                            googleFitDataModel.dataValueFour =
                                                                durationString
                                                            googleFitDataModel.dataFourMeasure =
                                                                "mins"
                                                        }
                                                        G_FIT_ACTIVITY_FIELD_DISTANCE -> {
                                                            val distanceFloatValue =
                                                                dataPoint.getValue(Field.FIELD_DISTANCE)
                                                                    .asFloat()
                                                            distanceCheck =
                                                                if (distanceCheck == 0f) distanceFloatValue else distanceCheck + distanceFloatValue
                                                            val distanceFinal =
                                                                (distanceCheck / 1609).toDouble() // meter to miles
                                                            val str1: String =
                                                                format(
                                                                    Locale.getDefault(),
                                                                    "%.2f",
                                                                    distanceFinal
                                                                )
                                                            googleFitDataModel.dataTypeThree =
                                                                "Distance"
                                                            googleFitDataModel.dataValueThree =
                                                                str1
                                                            googleFitDataModel.dataThreeMeasure =
                                                                "miles"
                                                        }
                                                        G_FIT_ACTIVITY_FIELD_STEPS -> {
                                                            val steps1 =
                                                                dataPoint.getValue(Field.FIELD_STEPS)
                                                                    .asInt()
                                                            stepCheck =
                                                                if (stepCheck == 0f) steps1.toFloat() else stepCheck + steps1
                                                            googleFitDataModel.dataTypeOne = "Steps"
                                                            googleFitDataModel.dataValueOne =
                                                                getWholeValue(
                                                                    stepCheck.toString()
                                                                        .replace(".0", "")
                                                                )
                                                            googleFitDataModel.dataOneMeasure =
                                                                "steps"
                                                        }
                                                    }
                                                }
                                            } catch (e: java.lang.Exception) {
                                                Log.i(
                                                    "##G-Fit",
                                                    "----->parseActivityDataResponseFromGFit------>" + e.message
                                                )
                                            }
                                        }
                                    }
                                    googleFitItemsList.add(googleFitDataModel)
                                }
                            } catch (e: java.lang.Exception) {
                                Log.i(
                                    "##G-Fit",
                                    "----->parseActivityDataResponseFromGFit------>" + e.message
                                )
                            }
                        }
                        mutableLiveData.setValue(googleFitItemsList)
                    } catch (e: java.lang.Exception) {
                        Log.d(
                            "##BraverGFit",
                            "----->parseActivityDataResponseFromGFit------>runOnUiThread---->" + e.message
                        )
                    }
                } else {
                    mutableLiveData.setValue(googleFitItemsList)
                }
            }
            .addOnFailureListener { e: java.lang.Exception ->
                Log.i(
                    "##BraverGFit",
                    "----->getHistoryClient-->getActivityData----->OnFailureListener---->" + e.message
                )
                mutableLiveData.setValue(googleFitItemsList)
            }
        return mutableLiveData
    }

    // Sleep
    private fun queryFitnessDataRequestForSleep(
        vitalsStartDate: String,
        vitalsEndDate: String
    ): SessionReadRequest? {
        return try {
            val sleepStartTime: Long = getNotificationTimeMills(vitalsStartDate)
            val sleepEndTime: Long = getNotificationTimeMills(vitalsEndDate)
            Log.i("##BraverGFit", "----->sleepStartTime---->$vitalsStartDate")
            Log.i("##BraverGFit", "----->sleepEndTime---->$vitalsEndDate")
            return SessionReadRequest.Builder().readSessionsFromAllApps().includeSleepSessions()
                .read(TYPE_SLEEP_SEGMENT)
                .setTimeInterval(sleepStartTime, sleepEndTime, TimeUnit.MILLISECONDS).build()
        } catch (e: Exception) {
            Log.i("##BraverGFit", "----->queryFitnessDataRequestForSleep---->" + e.message)
            null
        }
    }

    fun getSleepData(
        mActivity: Activity,
        startDate: String,
        endDate: String
    ): MutableLiveData<List<GoogleFitDataModel>> {
        Fitness.getSessionsClient(mActivity, getGoogleLastSignedInAccount(mActivity))
            .readSession(queryFitnessDataRequestForSleep(startDate, endDate)!!)
            .addOnSuccessListener { sessionReadResponse: SessionReadResponse? ->
                if (sessionReadResponse != null && sessionReadResponse.sessions.size > 0) {
                    googleFitItemsList.clear()
                    for (i in sessionReadResponse.sessions.indices) {
                        try {
                            val session: Session = sessionReadResponse.sessions[i]
                            if (session.activity == FitnessActivities.SLEEP) {
                                for (dataSet in sessionReadResponse.getDataSet(session)) {
                                    for (dp in dataSet.dataPoints) {
                                        val sleepStageVal =
                                            dp.getValue(Field.FIELD_SLEEP_SEGMENT_TYPE).asInt()
                                        val sleepStage: String =
                                            G_FIT_SLEEP_STAGES[sleepStageVal]
                                        val segmentStart: String = getDateFromLongForGoogleFit(
                                            dp.getStartTime(
                                                TimeUnit.MILLISECONDS
                                            )
                                        )
                                        val segmentEnd: String = getDateFromLongForGoogleFit(
                                            dp.getEndTime(
                                                TimeUnit.MILLISECONDS
                                            )
                                        )
                                        val between: String =
                                            getDifferenceTime(segmentStart, segmentEnd)
                                        val segmentTime = segmentStart.plus(" - ").plus(segmentEnd)
                                        if (sleepStage == "Light sleep") {
                                            googleFitItemsList.add(
                                                GoogleFitDataModel(
                                                    SLEEP, segmentTime,
                                                    "",
                                                    "", sleepStage, "", "", "", between, "",
                                                    "",
                                                    "", "", ""
                                                )
                                            )
                                        } else {
                                            googleFitItemsList.add(
                                                GoogleFitDataModel(
                                                    SLEEP, segmentTime,
                                                    "",
                                                    sleepStage, "", "", "", between, "", "",
                                                    "",
                                                    "", "", ""
                                                )
                                            )
                                        }

                                    }
                                }
                            }
                        } catch (e: java.lang.Exception) {
                            Log.d(
                                "##BraverGFit",
                                "----->parseSleepDataResponseFromGFit------>runOnUiThread---->" + e.message
                            )
                        }
                    }
                    mutableLiveData.setValue(googleFitItemsList)
                } else {
                    mutableLiveData.setValue(googleFitItemsList)
                }
            }
            .addOnFailureListener { e: java.lang.Exception ->
                Log.i(
                    "##BraverGFit",
                    "----->getHistoryClient-->getSleepData----->OnFailureListener---->" + e.message
                )
                mutableLiveData.setValue(googleFitItemsList)
            }
        return mutableLiveData
    }


    private fun getDifferenceTime(startTime: String, endTime: String): String {
        val dateStart = "$startTime:00"
        var totalSleep = ""
        val diffMinutes: Long
        val diffHours: Long
        val formatter =
            SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val d1: Date?
        val d2: Date?
        try {
            d1 = formatter.parse(dateStart)
            d2 = formatter.parse(endTime)
            val diff = d2.time - d1.time
            diffMinutes = diff / (60 * 1000) % 60
            diffHours = diff / (60 * 60 * 1000) % 24
            totalSleep = if (diffMinutes < 10 && diffHours >= 10) {
                diffHours.toString() + "h" + " 0" + diffMinutes + "m"
            } else if (diffHours < 10 && diffMinutes >= 10) {
                diffHours.toString() + "h" + " " + diffMinutes + "m"
            } else if (diffMinutes >= 10) {
                diffHours.toString() + "h" + " " + +diffMinutes + "m"
            } else {
                diffHours.toString() + "h" + " 0" + diffMinutes + "m"
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return totalSleep
    }
}