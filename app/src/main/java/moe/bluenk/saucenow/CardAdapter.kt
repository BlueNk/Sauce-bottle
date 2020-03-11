package moe.bluenk.saucenow

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_card.view.*

val pixivImageServer = arrayOf("i.pximg.net", "i1.pixiv.net", "i2.pixiv.net", "i3.pixiv.net", "i4.pixiv.net")

class CardAdapter(private val  jsonFeeds: JsonFeeds, val ctx: Context): RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val  layoutInflater = LayoutInflater.from(parent.context)
        val cardForRow = layoutInflater.inflate(R.layout.item_card, parent, false)
        return ViewHolder(cardForRow)
    }

    override fun getItemCount(): Int {
        return  jsonFeeds.results.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.view.stSimilarity.text = "${jsonFeeds.results[position].header.similarity} %"
        Picasso.get().load(jsonFeeds.results[position].header.thumbnail).into(holder.view.imgMain)

        // pixiv
        val indexPixiv = arrayOf(5, 51)
        if ( jsonFeeds.results[position].header.index_id in indexPixiv) {
            holder.view.stTitle.text =  jsonFeeds.results[position].data.title
            holder.view.stTinyTitle.text =  jsonFeeds.results[position].data.member_name
            holder.view.stSite.text = ctx.getString(R.string.pixiv)
            holder.source = jsonFeeds.results[position].data.ext_urls[0]
        }
        // Danbooru 9 / Gelbooru / yande.re 12
        val index3rd = arrayOf(9, 12)
        if ( jsonFeeds.results[position].header.index_id in index3rd ) {
            holder.view.stTitle.text = ctx.getString(R.string.title_empty)
            holder.view.stTinyTitle.text = jsonFeeds.results[position].data.creator.toString()

            var sourceURL = Uri.parse(jsonFeeds.results[position].data.source)
            when (sourceURL.host) {

                in pixivImageServer -> holder.view.stSite.text = ctx.getString(R.string.pixiv)
                "www.pixiv.net" -> holder.view.stSite.text = ctx.getString(R.string.pixiv)
                "twitter.com" -> holder.view.stSite.text = ctx.getString(R.string.twitter)
                "exhentai.org" -> holder.view.stSite.text = ctx.getString(R.string.exhentai)

                else -> {
                    holder.view.stSite.text = ctx.getString(R.string.source_empty)
                    Log.d("nonAddedLink", jsonFeeds.results[position].data.source)
                }
            }
            holder.ext_urls = jsonFeeds.results[position].data.ext_urls
            holder.source = jsonFeeds.results[position].data.source
        }
        // nico
        if ( jsonFeeds.results[position].header.index_id == 8 ) {
            holder.view.stTinyTitle.text = jsonFeeds.results[position].data.member_name
            holder.view.stSite.text = ctx.getString(R.string.nico)
        }

        // Nijie
        if ( jsonFeeds.results[position].header.index_id == 11 ) {
            holder.view.stTitle.text = jsonFeeds.results[position].data.title
            holder.view.stTinyTitle.text = jsonFeeds.results[position].data.member_name
            holder.view.stSite.text = ctx.getString(R.string.nijie)
            holder.source = jsonFeeds.results[position].data.ext_urls[0]
        }

    }

}

class  ViewHolder(val view: View, var source: String? = null, var ext_urls: List<String>? = null): RecyclerView.ViewHolder(view) {
    init {
        view.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)

            if (source != null) {
                val sourceURI:Uri = Uri.parse(source)
                if (sourceURI.toString().startsWith("http")) {
                    when (sourceURI.host) {
                        in pixivImageServer -> openURL.data = Uri.parse("https://www.pixiv.net/member_illust.php?mode=medium&illust_id=${sourceURI.lastPathSegment?.split("_")?.first()}")
                        else -> openURL.data = sourceURI
                    }
                    view.context.startActivity(openURL)
                } else {
                    Snackbar.make(view,"The source \"$source\",is not URL.", Snackbar.LENGTH_LONG).show()
                }
            } else {
                Snackbar.make(view,"What?! There's NO source here!", Snackbar.LENGTH_SHORT).show()
            }

        }

        view.setOnLongClickListener {
            var link: Uri
            if (source != null) {
                val sourceURI:Uri = Uri.parse(source)
                if (sourceURI.toString().startsWith("http")) {
                    link = when (sourceURI.host) {
                        in pixivImageServer -> Uri.parse("https://www.pixiv.net/member_illust.php?mode=medium&illust_id=${sourceURI.lastPathSegment?.split("_")?.first()}")
                        else -> sourceURI
                    }

                    val shareIntent = Intent.createChooser( Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, link.toString())
                        type = "text/*"

//                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }, null)

                    view.context.startActivity(shareIntent)

                } else {
                    Snackbar.make(view,"The source \"$source\",is not URL.", Snackbar.LENGTH_LONG).show()
                }
            } else {
                Snackbar.make(view,"What?! There's NO source here!", Snackbar.LENGTH_SHORT).show()
            }

            return@setOnLongClickListener true
        }
    }
}