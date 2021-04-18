package com.project.farmingapp.view.dashboard

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.service.autofill.UserData
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.with
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.farmingapp.R
import com.project.farmingapp.adapter.CurrentWeatherAdapter
import com.project.farmingapp.adapter.WeatherAdapter
import com.project.farmingapp.databinding.ActivityDashboardBinding
import com.project.farmingapp.model.WeatherApi
import com.project.farmingapp.model.data.Weather
import com.project.farmingapp.model.data.WeatherList
import com.project.farmingapp.model.data.WeatherRootList
import com.project.farmingapp.view.apmc.ApmcFragment
import com.project.farmingapp.view.articles.FruitsFragment
import com.project.farmingapp.view.auth.LoginActivity
import com.project.farmingapp.view.socialmedia.SocialMediaPostsFragment
import com.project.farmingapp.view.user.UserFragment
import com.project.farmingapp.view.weather.WeatherFragment
import com.project.farmingapp.viewmodel.UserDataViewModel
import com.project.farmingapp.viewmodel.WeatherViewModel
import com.squareup.picasso.Picasso
import com.squareup.picasso.PicassoProvider
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_weather.*
import kotlinx.android.synthetic.main.nav_header.*
import kotlinx.android.synthetic.main.nav_header.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var dashboardFragment: dashboardFragment
    lateinit var weatherFragment: WeatherFragment
    lateinit var navController: NavController
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var blankFragment1: WeatherFragment
    lateinit var apmcFragment: ApmcFragment
    lateinit var fruitsFragment: FruitsFragment
    lateinit var userFragment: UserFragment
    lateinit var socialMediaPostFragment: SocialMediaPostsFragment
    private lateinit var viewModel: UserDataViewModel
    val firebaseFireStore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()
    var userName = ""
    var data: WeatherRootList? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val binding: ActivityDashboardBinding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard)
        viewModel = ViewModelProviders.of(this).get(UserDataViewModel::class.java)
        binding.userDataViewModel = viewModel


        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        viewModel.getUserData(firebaseAuth.currentUser!!.email as String)

        navView.setNavigationItemSelectedListener(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dashboardFragment = dashboardFragment()
        weatherFragment = WeatherFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, dashboardFragment, "userDash")
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .setReorderingAllowed(true)
            .addToBackStack("userDash")
            .commit()

        bottomNav.selectedItemId = R.id.bottomNavHome

        val something = navView.getHeaderView(0);

        if (dashboardFragment.isVisible) {
            bottomNav.selectedItemId = R.id.bottomNavHome
        }

//        val googleLoggedUserName = firebaseAuth.currentUser!!.displayName
//        if (googleLoggedUserName.isNullOrEmpty()) {
//            firebaseFireStore.collection("users").document(firebaseAuth.currentUser!!.email!!)
//                .get()
//                .addOnCompleteListener {
//                    val data = it.result
//                    userName = data!!.getString("name").toString()
//                    something.cityTextNavHeader.text = data!!.getString("city").toString()
//                    something.navbarUserName.text = userName
//                }
//        } else {
//            something.navbarUserName.text = googleLoggedUserName
//        }


//        getWeather()

        something.setOnClickListener {
            Toast.makeText(this, "You Clicked Slider", Toast.LENGTH_LONG).show()

            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                super.onBackPressed()
            }
            userFragment = UserFragment()
            setCurrentFragment(userFragment)
        }
        apmcFragment = ApmcFragment()
        socialMediaPostFragment = SocialMediaPostsFragment()

        bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottomNavAPMC -> setCurrentFragment(apmcFragment)
                R.id.bottomNavHome -> setCurrentFragment(dashboardFragment)
                R.id.bottomNavEcomm -> setCurrentFragment(dashboardFragment)
                R.id.bottomNavPost -> setCurrentFragment(socialMediaPostFragment)
            }
            true
        }

        viewModel.userliveData.observe(this, Observer {
            val something = navView.getHeaderView(0);
            userName = it.get("name").toString()
            something.cityTextNavHeader.text ="City: " +  it.get("city").toString()
            something.navbarUserName.text = userName
            something.navbarUserEmail.text = firebaseAuth.currentUser!!.email
            Glide.with(this).load(it.get("profileImage")).into(something.navbarUserImage)
            Log.d("User Data from VM", it.getString("name"))
            val posts = it.get("posts") as List<String>

            something.navBarUserPostCount.text = "Posts Count: " + posts.size.toString()
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frame_layout, fragment)

            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            setReorderingAllowed(true)
            addToBackStack("name")
            commit()
        }
    }


    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        toggle.syncState()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        bottomNav.selectedItemId = R.id.bottomNavHome
        when (item.itemId) {

            R.id.miItem1 -> {
                if (supportFragmentManager.findFragmentByTag("name") == null) {
                    fruitsFragment = FruitsFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_layout, fruitsFragment, "article")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .setReorderingAllowed(true)
                        .addToBackStack("article")
                        .commit()
                }
            }

            R.id.miItem4 -> {
                if (supportFragmentManager.findFragmentByTag("name") == null) {
                    apmcFragment = ApmcFragment()
                    bottomNav.selectedItemId = R.id.bottomNavAPMC
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_layout, apmcFragment, "name1")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .setReorderingAllowed(true)
                        .addToBackStack("name")
                        .commit()
                }
            }
            R.id.miItem8 -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Log Out")
                    .setMessage("Do you want to logout?")
                    .setPositiveButton("Yes") { dialogInterface, i ->
                        firebaseAuth.signOut()
                        Toast.makeText(this, "Logged Out", Toast.LENGTH_LONG).show()
                        Intent(this, LoginActivity::class.java).also {
                            startActivity(it)
                        }
                    }
                    .setNegativeButton("No") { dialogInterface, i ->
                    }
                    .show()
            }
            R.id.miItem7 -> {

            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (dashboardFragment.isVisible) {
//            Toast.makeText(this, "A", Toast.LENGTH_LONG).show()
//            bottomNav.selectedItemId = R.id.bottomNavHome
        }

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

}