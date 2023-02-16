package com.qlive.qnlivekit

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.qlive.sdk.QLive
import com.qlive.sdk.QUserInfo
import com.qlive.core.QLiveCallBack
import com.qlive.qnlivekit.App.Companion.demo_url
import com.qlive.qnlivekit.databinding.ActivityMainBinding
import com.qlive.qnlivekit.uitil.*
import com.qlive.uikitcore.activity.BaseBindingActivity
import com.qlive.uikitcore.dialog.LoadingDialog
import com.qlive.uikitcore.ext.permission.PermissionAnywhere
import com.qlive.uikitcore.ext.permission.PermissionCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.Request
import okio.Buffer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MainActivity : BaseBindingActivity<ActivityMainBinding>() {

    override fun init() {
        PermissionAnywhere.requestPermission(this, arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        ) { _, _, _ -> }

        //登陆按钮
        binding.btLoginLogin.setOnClickListener {

            val phone = binding.etLoginPhone.text.toString() ?: ""
            val code = binding.etLoginVerificationCode.text.toString() ?: ""
            if (phone.isEmpty()) {
                Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (code.isEmpty()) {
                Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!binding.cbAgreement.isSelected) {
                Toast.makeText(this, "请同意 七牛云服务用户协议 和 隐私权政策", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
//            QLive.getLiveUIKit().getPage(RoomPage::class.java).anchorCustomLayoutID = R.layout.my_activity_room_pusher
            lifecycleScope.launch {
                LoadingDialog.showLoading(supportFragmentManager)
                try {
                    //demo登陆
                    login(phone, code)
                    SpUtil.get("login").saveData("phone", phone)
                    //登陆
                    auth()
                    //绑定用户信息
                    suspendSetUser()
                    //启动跳转到直播列表
                    startActivity(Intent(this@MainActivity, DemoSelectActivity::class.java))
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    LoadingDialog.cancelLoadingDialog()
                }
            }
        }
        initOtherView()
        val lastPhone = SpUtil.get("login").readString("phone", "")
        if (!TextUtils.isEmpty(lastPhone)) {
            binding.etLoginPhone.setText(lastPhone)
            binding.etLoginVerificationCode.setText("8888")
        }
    }

    private suspend fun auth() = suspendCoroutine<Unit> { coroutine ->
        QLive.auth(object : QLiveCallBack<Void> {
            override fun onError(code: Int, msg: String?) {
                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                coroutine.resumeWithException(Exception("getTokenError"))
            }

            override fun onSuccess(data: Void?) {
                coroutine.resume(Unit)
            }
        })
    }

    /**
     *  //绑定用户信息 绑定后房间在线用户能返回绑定设置的字段
     */
    suspend fun suspendSetUser() =
        suspendCoroutine<Unit> { coroutine ->
            //绑定用户信息 绑定后房间在线用户能返回绑定设置的字段
            QLive.setUser(QUserInfo().apply {
                // avatar ="https://cdn2.jianshu.io/assets/default_avatar/14-0651acff782e7a18653d7530d6b27661.jpg"
                avatar = UserManager.user!!.data.avatar //设置当前用户头像
                nick = UserManager.user!!.data.nickname //设置当前用户昵称
                extension = HashMap<String, String>().apply {
                    put("phone", "13141616037")
                    put("customFiled", "i am customFile")
                }
            }, object : QLiveCallBack<Void> {
                override fun onError(code: Int, msg: String?) {
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    coroutine.resumeWithException(Exception("getTokenError"))
                }

                override fun onSuccess(data: Void?) {
                    coroutine.resume(Unit)
                }
            })
        }

    //demo自己的登陆
    suspend fun login(phoneNumber: String, smsCode: String) = suspendCoroutine<Unit> { ct ->
        Thread {
            try {
                val body = FormBody.Builder()
                    .add("phone", phoneNumber)
                    .add("smsCode", smsCode)
                    .build()
                val buffer = Buffer()
                body.writeTo(buffer)

                val request = Request.Builder()
                    .url("${demo_url}/v1/signUpOrIn")
                    //  .addHeader(headerKey, headerValue)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .post(body)
                    .build();
                val call = OKHttpManger.okHttp.newCall(request);
                val resp = call.execute()

                val code = resp.code
                val userJson = resp.body?.string()
                val user = JsonUtils.parseObject(userJson, BZUser::class.java)
                UserManager.onLogin(user!!)
                ct.resume(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                ct.resumeWithException(Exception(e.message))
            }
        }.start()
    }

    private fun timeJob() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                binding.tvSmsTime.isClickable = false
                repeat(60) {
                    binding.tvSmsTime.text = (60 - it).toString()
                    delay(1000)
                }
                binding.tvSmsTime.text = "获取验证码"
                binding.tvSmsTime.isClickable = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initOtherView() {
        binding.tvSmsTime.setOnClickListener {
            val phone = binding.etLoginPhone.text.toString() ?: ""
            if (phone.isEmpty()) {
                return@setOnClickListener
            }
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val body = FormBody.Builder()
                        .add("phone", phone)
                        .build()
                    val buffer = Buffer()
                    body.writeTo(buffer)
                    val request = Request.Builder()
                        .url("${demo_url}/v1/getSmsCode")
                        //  .addHeader(headerKey, headerValue)
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .post(body)
                        .build();
                    val call = OKHttpManger.okHttp.newCall(request);
                    val resp = call.execute()
                    timeJob()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        binding.cbAgreement.setOnClickListener {
            binding.cbAgreement.isSelected = !binding.cbAgreement.isSelected
        }
        val tips = "我已阅读并同意 七牛云服务用户协议 和 隐私权政策"
        val spannableString = SpannableString(tips)
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                WebActivity.start("https://www.qiniu.com/privacy-right", this@MainActivity)
            }
        }, tips.length - 5, tips.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                WebActivity.start("https://www.qiniu.com/user-agreement", this@MainActivity)
            }
        }, tips.length - 18, tips.length - 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            tips.length - 5,
            tips.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            tips.length - 18,
            tips.length - 7,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#007AFF")),
            tips.length - 5,
            tips.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#007AFF")),
            tips.length - 18,
            tips.length - 7,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.cbAgreement.movementMethod = LinkMovementMethod.getInstance();//设置可点击状态
        binding.cbAgreement.text = spannableString
    }
}