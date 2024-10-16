package com.screen.record.screens.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.screen.record.R
import com.screen.record.screens.home.HomeScreen
import com.screen.record.screens.signIn.SignInScreen
import com.screen.record.utils.USER_DETAILS
import com.screen.record.utils.loadScreen
import com.screen.record.utils.prefs

class SplashScreen : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.e("Test","User Details - ${prefs.get(USER_DETAILS, "")}")

        Handler(Looper.getMainLooper()).postDelayed({
            if(prefs.get(USER_DETAILS,"")!="") {
                loadScreen(requireActivity(), HomeScreen())
            } else {
                loadScreen(requireActivity(), SignInScreen())
            }
        }, 5000)
    }

    override fun onDetach() {
        super.onDetach()
    }

}