package com.example.keepthetime_project

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.keepthetime_project.databinding.ActivityMainBinding
import com.example.keepthetime_project.databinding.ActivitySignInBinding
import com.example.keepthetime_project.datas.BasicResponse
import com.example.keepthetime_project.utils.ContextUtil
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInActivity : BaseActivity() {

    private lateinit var binding : ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setEvents()
        setValues()

    }

    override fun setEvents() {
        binding.btnLogin.setOnClickListener {
            val inputEmail = binding.edtEmail.text.toString()
            val inputPw = binding.edtPw.text.toString()

            apiList.postRequestLogin(inputEmail, inputPw).enqueue(object : Callback<BasicResponse>{
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    if(response.isSuccessful){
                        response.body()?.let {
                            Toast.makeText(mContext, "${it.data.user.nick_name} 님 환영합니다.", Toast.LENGTH_SHORT).show()

                            ContextUtil.setLoginUserToken(mContext, it.data.token)

                            val myIntent = Intent(mContext, MainActivity::class.java)
                            startActivity(myIntent)
                            finish()

                        }
                    }else{
                        response.errorBody()?.let {
                            val jsonObj = JSONObject(response.errorBody().toString())
                            val message = jsonObj.getString("message")
                            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
                        }
                    }

                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {

                }

            })

        }


        binding.btnSignup.setOnClickListener {
            val myIntent = Intent(mContext, SignupActivity::class.java)
            startActivity(myIntent)
        }

    }



    override fun setValues() {

    }
}