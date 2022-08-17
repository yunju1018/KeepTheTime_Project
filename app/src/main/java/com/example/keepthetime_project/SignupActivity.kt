package com.example.keepthetime_project

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.keepthetime_project.databinding.ActivitySignupBinding
import com.example.keepthetime_project.datas.BasicResponse
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupActivity : BaseActivity() {

    private lateinit var binding : ActivitySignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setEvents()
        setValues()


    }

    override fun setEvents() {
        binding.btnSingUp.setOnClickListener {

            val inputEmail = binding.edtEmail.text.toString()
            val inputPw = binding.edtPassword.text.toString()
            val inputNickname = binding.edtNickname.text.toString()

            apiList.putRequestSignup(inputEmail,inputPw,inputNickname).enqueue(object : Callback<BasicResponse>{
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    if(response.isSuccessful){
                        response.body()?.let{
                            Toast.makeText(mContext, "회원가입에 성공했습니다.", Toast.LENGTH_SHORT).show()
                            Log.d("yj", it.message)
                            finish()
                        }
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {

                }

            })



        }

        binding.btnEmailCheck.setOnClickListener {

            val inputEmail = binding.edtEmail.text.toString()

            if(inputEmail.isEmpty()){
                Toast.makeText(mContext, "이메일을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else{
                apiList.getRequestUserCheck("EMAIL", inputEmail).enqueue(object : Callback<BasicResponse>{
                    override fun onResponse(
                        call: Call<BasicResponse>,
                        response: Response<BasicResponse>
                    ) {
                        if(response.isSuccessful){
                            response.body()?.let {
                                Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
                                Log.d("yj", it.message)
                            }
                        }else{
                            response.errorBody()?.let {
                                val jsonObj = JSONObject(it.string())
                                val message = jsonObj.getString("message")

                                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
                                Log.d("yj", message)

                            }
                        }
                    }

                    override fun onFailure(call: Call<BasicResponse>, t: Throwable) {

                    }

                })
            }
            

            
        }

    }

    override fun setValues() {

    }
}