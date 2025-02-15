package com.liamfarrell.android.snapbattle.mvvm_ui.startup

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.liamfarrell.android.snapbattle.R
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.activity_startup.*
import kotlinx.android.synthetic.main.toolbar.view.*
import javax.inject.Inject

interface SetupToolbarInterface {
    fun setTitle(title: String)
    fun enableNextButton()
    fun disableNextButton()
    fun hideToolbar()
    fun showToolbar()
}

class StartupActivity : AppCompatActivity(), SetupToolbarInterface, HasAndroidInjector {


    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    private var enableNextButton = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)
        setToolbar(toolbar.toolbar)
    }

    override fun androidInjector() = dispatchingAndroidInjector

    private fun setToolbar(toolbar : androidx.appcompat.widget.Toolbar){
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_startup, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_next)?.isEnabled = enableNextButton
        return true
    }

    override fun setTitle(title: String) {
        supportActionBar?.title = title
    }

    @SuppressLint("RestrictedApi")
    override fun enableNextButton(){
        enableNextButton = true
        supportActionBar?.invalidateOptionsMenu()
    }

    @SuppressLint("RestrictedApi")
    override fun disableNextButton(){
        enableNextButton = false
        supportActionBar?.invalidateOptionsMenu()
    }

    override fun hideToolbar() {
        supportActionBar?.hide()
    }

    override fun showToolbar() {
        supportActionBar?.show()
    }



}
