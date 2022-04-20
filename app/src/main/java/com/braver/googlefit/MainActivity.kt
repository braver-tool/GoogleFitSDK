package com.braver.googlefit

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AnticipateInterpolator
import android.widget.DatePicker
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import com.braver.googlefit.MainViewModel.Companion.GOOGLE_FIT_PERMISSIONS_REQUEST_CODE
import com.braver.googlefit.MainViewModel.Companion.IS_GOOGLE_AUTH_SIGNED
import com.braver.googlefit.MainViewModel.Companion.USER_NAME
import com.braver.googlefit.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.fitness.FitnessOptions
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private var filterOption: String = ""
    private var isClickSignIn: Boolean = false
    private var isDataShown: Boolean = false
    private var prefManager: PreferenceUtils? = null
    private var googleFitDataList: MutableList<GoogleFitDataModel> = ArrayList()
    private var googleFitAdapter: GoogleFitAdapter? = null
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            customizeSplashScreenExitAnimation()
        }
        keepSplashScreenForLongerPeriod()
        initializeViews()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, R.string.msg_pair_success, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, R.string.alert_denied_permission, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.gFitStartDateAnsTextView -> {
                openCalendarPicker(true, "")
            }
            R.id.gFitEndDateAnsTextView -> {
                onClickEndDateField()
            }
            R.id.syncDataTextView -> {
                onClickSyncButton()
            }
            R.id.gFitLoginImageView -> {
                onClickUserIcon()
            }
        }
    }

    private fun onClickEndDateField() {
        val startDate: String = binding.gFitStartDateAnsTextView.text.toString()
        if (startDate.isNotEmpty()) {
            openCalendarPicker(false, startDate)
        } else {
            Toast.makeText(this, R.string.alert_start_date, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onClickUserIcon() {
        if (!prefManager!!.getBooleanValue(IS_GOOGLE_AUTH_SIGNED)) {
            if (!isClickSignIn) {
                isClickSignIn = true
                onClickGoogleSignInButton()
            }
        } else {
            showAlertDialog()
        }
    }

    private fun onClickSyncButton() {
        if (isDataShown) {
            changeUI(false)
        } else {
            if (!prefManager!!.getBooleanValue(IS_GOOGLE_AUTH_SIGNED)) {
                Toast.makeText(
                    this,
                    R.string.alert_google_auth,
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else if (filterOption.isEmpty()) {
                Toast.makeText(this, R.string.alert_module, Toast.LENGTH_SHORT).show()
            } else if (binding.gFitStartDateAnsTextView.text.toString().isEmpty()) {
                Toast.makeText(this, R.string.alert_start_date, Toast.LENGTH_SHORT).show()
            } else if (binding.gFitStartDateAnsTextView.text.toString().isEmpty()) {
                Toast.makeText(this, R.string.alert_end_date, Toast.LENGTH_SHORT).show()
            } else if (!mainViewModel.isGoogleFitPermissionGranted(this)) {
                requestGoogleFitPermission(mainViewModel.getGoogleLastSignedInAccount(this))
            } else {
                if (checkActivityRecoPermission()) {
                    syncDataFromGoogleFitApp()
                }
            }
        }
    }

    private fun checkActivityRecoPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return true
        }
        return when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) -> {
                true
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                false
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                syncDataFromGoogleFitApp()
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q && (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ))
            ) {
                //showPermissionDialogForAndroidDeniedPermissions()
                Log.d(
                    "##Activity_Recognition",
                    "----------->Need to navigate to settings to enable Activity_Recognition permission manually"
                )
            } else {
                Log.d(
                    "##Activity_Recognition",
                    "----------->Permission denied by the user"
                )
            }
        }

    private fun showAlertDialog() {
        val alertDialog: AlertDialog? = let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(
                    R.string.alert_yes
                ) { dialog, _ ->
                    removeGoogleFitAccess(this@MainActivity)
                    dialog.dismiss()
                }
                setNegativeButton(
                    R.string.alert_no
                ) { dialog, _ ->
                    dialog.dismiss()
                }
                setTitle(R.string.logout)
                setMessage(R.string.alert_logout)
            }
            builder.create()
        }
        alertDialog!!.show()
    }

    // Revoke Fitness permissions
    private fun removeGoogleFitAccess(context: Context?) {
        try {
            val signInOptions = GoogleSignInOptions.Builder()
                .addExtension(mainViewModel.getFitnessSignInOptions()).build()
            GoogleSignIn.getClient(context!!, signInOptions).revokeAccess()
            val mGoogleSignInClient = mainViewModel.getGoogleSignInClient(this@MainActivity)
            mGoogleSignInClient.signOut()
            binding.gFitRadioGroup.clearCheck()
            filterOption = ""
            binding.gFitStartDateAnsTextView.text = ""
            binding.gFitEndDateAnsTextView.text = ""
            binding.gUserNameTextView.text = ""
            binding.gUserNameTextView.visibility = View.GONE
            prefManager!!.setBooleanValue(IS_GOOGLE_AUTH_SIGNED, false)
            prefManager!!.setStringValue(USER_NAME, "")
            binding.gFitLoginImageView.setImageResource(R.drawable.ic_account_circle_outlined)
            isClickSignIn = false
            if (isDataShown) {
                changeUI(false)
            }
        } catch (e: Exception) {
            Log.d("###removeGoogleFitAccess :", "------------------->" + e.message)
        }
    }


    private fun syncDataFromGoogleFitApp() {
        val startDate: String = binding.gFitStartDateAnsTextView.text.toString() + " 00:00:00"
        val endDate: String = binding.gFitEndDateAnsTextView.text.toString() + " 23:59:59"
        when (filterOption) {
            "Vitals" -> {
                binding.progressCircular.visibility = View.VISIBLE
                mainViewModel.getVitalsData(this@MainActivity, startDate, endDate)
                    .observe(this) { vitalsDataList ->
                        if (vitalsDataList.isNotEmpty()) {
                            binding.noRecordsTextView.visibility = View.GONE
                            googleFitDataList = ArrayList()
                            googleFitDataList.addAll(vitalsDataList)
                            googleFitAdapter = GoogleFitAdapter(googleFitDataList)
                            binding.gFitRecyclerView.adapter = googleFitAdapter
                            changeUI(true)
                        } else {
                            binding.noRecordsTextView.visibility = View.VISIBLE
                            changeUI(false)
                        }
                        binding.progressCircular.visibility = View.GONE
                    }
            }
            "Activity" -> {
                binding.progressCircular.visibility = View.VISIBLE
                mainViewModel.getActivityData(this@MainActivity, startDate, endDate)
                    .observe(this) { vitalsDataList ->
                        if (vitalsDataList.isNotEmpty()) {
                            binding.noRecordsTextView.visibility = View.GONE
                            googleFitDataList = ArrayList()
                            googleFitDataList.addAll(vitalsDataList)
                            googleFitAdapter = GoogleFitAdapter(googleFitDataList)
                            binding.gFitRecyclerView.adapter = googleFitAdapter
                            changeUI(true)
                        } else {
                            binding.noRecordsTextView.visibility = View.VISIBLE
                            changeUI(false)
                        }
                        binding.progressCircular.visibility = View.GONE
                    }
            }
            "Sleep" -> {
                binding.progressCircular.visibility = View.VISIBLE
                mainViewModel.getSleepData(this@MainActivity, startDate, endDate)
                    .observe(this) { vitalsDataList ->
                        if (vitalsDataList.isNotEmpty()) {
                            binding.noRecordsTextView.visibility = View.GONE
                            googleFitDataList = ArrayList()
                            googleFitDataList.addAll(vitalsDataList)
                            googleFitAdapter = GoogleFitAdapter(googleFitDataList)
                            binding.gFitRecyclerView.adapter = googleFitAdapter
                            changeUI(true)
                        } else {
                            binding.noRecordsTextView.visibility = View.VISIBLE
                            changeUI(false)
                        }
                        binding.progressCircular.visibility = View.GONE
                    }
            }
        }
    }

    private fun changeUI(isData: Boolean) {
        isDataShown = isData
        if (isDataShown) {
            binding.syncDataTextView.text = resources.getString(R.string.clear_data)
            binding.chooserTitleTextView.text = resources.getString(R.string.synced_data)
            for (index in 0 until binding.gFitRadioGroup.childCount)
                binding.gFitRadioGroup.getChildAt(index).isEnabled = false
            binding.gFitStartDateAnsTextView.isEnabled = false
            binding.gFitEndDateAnsTextView.isEnabled = false
        } else {
            binding.gFitRadioGroup.clearCheck()
            filterOption = ""
            binding.gFitStartDateAnsTextView.text = ""
            binding.gFitEndDateAnsTextView.text = ""
            googleFitDataList.clear()
            googleFitAdapter!!.notifyDataSetChanged()
            binding.syncDataTextView.text = resources.getString(R.string.sync_data)
            binding.chooserTitleTextView.text = resources.getString(R.string.chooser_title)
            for (index in 0 until binding.gFitRadioGroup.childCount)
                binding.gFitRadioGroup.getChildAt(index).isEnabled = true
            binding.gFitStartDateAnsTextView.isEnabled = true
            binding.gFitEndDateAnsTextView.isEnabled = true
        }
    }

    private fun onClickGoogleSignInButton() {
        val mGoogleSignInClient = mainViewModel.getGoogleSignInClient(this@MainActivity)
        val signInIntent = mGoogleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                // The Task returned from this call is always completed, no need to attach
                // a listener.
                val completedTask = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    // Signed in successfully, show authenticated UI.
                    val account: GoogleSignInAccount =
                        completedTask.getResult(ApiException::class.java)
                    setUserName(account.displayName!!)
                    prefManager!!.setBooleanValue(IS_GOOGLE_AUTH_SIGNED, true)
                    //requestGoogleFitPermission(account)
                    Toast.makeText(this, R.string.alert_auth_success, Toast.LENGTH_SHORT)
                        .show()
                } catch (e: ApiException) {
                    // The ApiException status code indicates the detailed failure reason.
                    // Please refer to the GoogleSignInStatusCodes class reference for more information.
                    isClickSignIn = false
                    prefManager!!.setBooleanValue(IS_GOOGLE_AUTH_SIGNED, false)
                    Toast.makeText(this, R.string.alert_auth_failed, Toast.LENGTH_SHORT).show()
                }
            } else {
                isClickSignIn = false
                Log.d("##mGoogleSignInClient", "----------->Failed")
            }
        }

    private fun setUserName(userName: String) {
        val userNameFormat =
            Character.toUpperCase(userName[0]).toString() + userName.substring(1).lowercase()
        binding.gUserNameTextView.text = userNameFormat
        binding.gUserNameTextView.visibility = View.VISIBLE
        prefManager!!.setStringValue(USER_NAME, userNameFormat)
        binding.gFitLoginImageView.setImageResource(R.drawable.ic_account_circle_filled)
    }

    private fun requestGoogleFitPermission(account: GoogleSignInAccount) {
        val fitnessOptions: FitnessOptions = mainViewModel.getFitnessSignInOptions()
        GoogleSignIn.requestPermissions(
            this,
            GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
            account,
            fitnessOptions
        )
    }

    private fun openCalendarPicker(isStartDate: Boolean, minimumDate: String) {
        val c = Calendar.getInstance()
        val mYear = c[Calendar.YEAR]
        val mMonth = c[Calendar.MONTH]
        val mDay = c[Calendar.DAY_OF_MONTH]
        val datePickerDialog = DatePickerDialog(
            this,
            { view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                if (dayOfMonth > c[Calendar.DAY_OF_MONTH] && year == c[Calendar.YEAR] && monthOfYear == c[Calendar.MONTH]
                ) view.updateDate(
                    c[Calendar.YEAR],
                    c[Calendar.MONTH],
                    c[Calendar.DAY_OF_MONTH]
                )
                val selectedMont: Int = monthOfYear + 1
                val date: String = if (dayOfMonth < 10) {
                    "0$dayOfMonth"
                } else {
                    dayOfMonth.toString()
                }
                val month: String = if (selectedMont < 10) {
                    "0$selectedMont"
                } else {
                    selectedMont.toString()
                }
                val formattedDate = "$year-$month-$date"
                if (isStartDate) {
                    binding.gFitStartDateAnsTextView.text = formattedDate
                } else {
                    binding.gFitEndDateAnsTextView.text = formattedDate
                }
            }, mYear, mMonth, mDay
        )
        if (minimumDate.isNotEmpty() && !isStartDate) {
            datePickerDialog.datePicker.minDate = mainViewModel.convertToDate(minimumDate)
        }
        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePickerDialog.show()
    }


    private fun initializeViews() {
        prefManager = PreferenceUtils.getInstance(this)
        val linearLayoutManager = LinearLayoutManager(this)
        binding.gFitRecyclerView.layoutManager = linearLayoutManager
        binding.gFitRecyclerView.setHasFixedSize(true)
        binding.gFitStartDateAnsTextView.setOnClickListener(this)
        binding.gFitEndDateAnsTextView.setOnClickListener(this)
        binding.syncDataTextView.setOnClickListener(this)
        binding.gFitLoginImageView.setOnClickListener(this)
        binding.gFitRadioGroup.setOnCheckedChangeListener { group: RadioGroup, checkedId: Int ->
            val childCount = group.childCount
            for (x in 0 until childCount) {
                val btn = group.getChildAt(x) as RadioButton
                if (btn.id == checkedId) {
                    filterOption = btn.text.toString()
                }
            }
        }
        binding.gUserNameTextView.text = prefManager!!.getStringValue(USER_NAME)
        binding.gUserNameTextView.visibility =
            if (prefManager!!.getStringValue(USER_NAME)!!.isEmpty()) View.GONE else View.VISIBLE
        binding.gFitLoginImageView.setImageResource(
            if (prefManager!!.getStringValue(USER_NAME)!!
                    .isEmpty()
            ) R.drawable.ic_account_circle_outlined else R.drawable.ic_account_circle_filled
        )
    }

    private fun keepSplashScreenForLongerPeriod() {
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (mainViewModel.isDataReady()) {
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        false
                    }
                }
            }
        )
    }

    private fun customizeSplashScreenExitAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                /** Exit immediately **/
                //splashScreenView.remove()
                /** Exit using animation after particular duration **/
                val slideLeft = ObjectAnimator.ofFloat(
                    splashScreenView,
                    View.TRANSLATION_X,
                    0f,
                    -splashScreenView.width.toFloat()
                )
                slideLeft?.interpolator = AnticipateInterpolator()
                slideLeft?.duration = 500L
                slideLeft?.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        splashScreenView.remove()
                    }
                })
                slideLeft?.start()
            }
        }
    }
}