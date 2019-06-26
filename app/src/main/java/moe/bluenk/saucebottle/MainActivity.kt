package moe.bluenk.saucebottle

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.*
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.florent37.expectanim.ExpectAnim
import com.github.florent37.expectanim.core.Expectations.*
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.btnImage
import kotlinx.android.synthetic.main.activity_main.imgView
import kotlinx.android.synthetic.main.activity_main.ptUrl
import okhttp3.*
import java.io.*

class MainActivity : AppCompatActivity() {

    val okhttp = OkHttpClient()
    var iconLink = true
    private var requestUrl = "https://saucenao.com/search.php?output_type=2&numres=8"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        recyclerView.layoutManager = LinearLayoutManager(this)

//        val anim = ExpectAnim()
//            .expect(imgView)
//            .toBe(
//                scale(0.55f, 0.55f), leftOfParent(), bottomOfParent()
//        ).toAnimation()
//
//        var absolutOffset = 0
//        val startPoint = 600
//        appbarLayout.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener{
//
//            override fun onOffsetChanged(p0: AppBarLayout?, offset: Int) {
//                absolutOffset = -offset - startPoint
//                var percent = (absolutOffset * 1f) / (925 - startPoint)
//                anim.setPercent(percent)
//                Log.d("Offset", percent.toString())
//            }
//        })

        when { intent?.action == Intent.ACTION_SEND -> {

            if ("text/plain" == intent.type) {
                val imageUrl = handleSendText(intent)
                val requestByUrl = Request.Builder().url("$requestUrl&url=$imageUrl").build()

                Picasso.get().load(imageUrl).into(imgView)
                getResponJson(requestByUrl)

            } else if (intent.type?.startsWith("image/") == true) {
                val imageByteArray = uriToByteArray(handleSendImage(intent))
                val bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.count())

                val requestBody: RequestBody = RequestBody.create(MediaType.parse("image/*"), imageByteArray)
                val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("file", "image.bmp", requestBody).build()
                val requestByImage = Request.Builder().url(requestUrl).post(multipartBody).build()

                imgView.setImageBitmap(bitmap)
                getResponJson(requestByImage)
            }
        }
        }

        btnImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"

            startActivityForResult(intent, 1)
        }

        fab.setOnClickListener {
            val url = ptUrl.text.toString()
            val requestByUrl = Request.Builder().url("$requestUrl&url=$url").build()

            ptUrl.clearFocus()

            Picasso.get().load(Uri.parse(url)).into(imgView)
            getResponJson(requestByUrl)
        }
    }

    private fun getResponJson(request: Request) {
        enSearchView.start()

        okhttp.newCall(request).enqueue(object :  Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                val json = Gson().fromJson(body,  JsonFeeds::class.java)

                if (json.header.status > 0) {
                    Snackbar.make(mainCoordinatorLayout, getString(R.string.sanckWrongServer), Snackbar.LENGTH_LONG).show()
                    runOnUiThread {
                        recyclerView.adapter = CardAdapter(json, applicationContext)
                    }
                } else if (json.header.status < 0) {
                    Snackbar.make(mainCoordinatorLayout, getString(R.string.sanckWrongURL), Snackbar.LENGTH_LONG).show()
                } else {
                    runOnUiThread {
                        recyclerView.adapter = CardAdapter(json, applicationContext)
                    }
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                Snackbar.make(mainCoordinatorLayout, "Error 1, Please retry  ", Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun handleSendText(intent: Intent): String?{
        return intent.getStringExtra(Intent.EXTRA_TEXT)
    }

    private fun handleSendImage(intent: Intent): Uri{
        return intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Log.d("MainActivity", "Get local image!")
            val localImageUri = data?.data

            imgView.setImageURI(localImageUri)

            val requestBody: RequestBody = RequestBody.create(MediaType.parse("image/*"), uriToByteArray(localImageUri!!))
            val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("file", "image.bmp", requestBody).build()
            val requestByImage = Request.Builder().url(requestUrl).post(multipartBody).build()

            getResponJson(requestByImage)
        }
    }

    private fun uriToByteArray(uri: Uri): ByteArray {
        val imageInputStream= contentResolver.openInputStream(uri)
        val byteArrayOutputStream = ByteArrayOutputStream()

        imageInputStream.use { input ->
            byteArrayOutputStream.use { output ->
                input?.copyTo(output)
            }
        }

        imageInputStream?.close()
        byteArrayOutputStream.close()
        return byteArrayOutputStream.toByteArray()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Snackbar.make(mainCoordinatorLayout,"button clicked!",Snackbar.LENGTH_SHORT).show()
                return true
            }
            R.id.action_searchBy -> {
                when (iconLink) {
                    true -> {
                        item.setIcon(R.drawable.ic_image)
                        item.setTitle(R.string.searchByImage)
                        iconLink = false
                        ptUrl.visibility  = View.VISIBLE
                        btnImage.visibility =View.GONE
                        enSearchView.visibility = View.VISIBLE
                        fab.show()
                    }
                    false -> {
                        item.setIcon(R.drawable.ic_link)
                        item.setTitle(R.string.searchByURL)
                        iconLink = true
                        ptUrl.visibility  = View.GONE
                        btnImage.visibility =View.VISIBLE
                        fab.hide()
                        enSearchView.visibility = View.GONE
                    }
                }
                return  true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

class StopDragBehavior(context: Context, attrs: AttributeSet) : AppBarLayout.Behavior(context, attrs) {
    init {
        setDragCallback(object : DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean = false
        })
    }
}