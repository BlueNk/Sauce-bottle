package moe.bluenk.saucebottle

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_card.view.*

val pixivImageServer = arrayOf("i.pximg.net", "i2.pixiv.net")

class CardAdapter(val  jsonfeed: JsonFeeds ,val ctx: Context): RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val  layoutInflater = LayoutInflater.from(parent.context)
        val cardForRow = layoutInflater.inflate(R.layout.item_card, parent, false)
        return ViewHolder(cardForRow)
    }

    override fun getItemCount(): Int {
        return  jsonfeed.results.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.view.stSimilarity.text = "${jsonfeed.results[position].header.similarity} %"
        Picasso.get().load(jsonfeed.results[position].header.thumbnail).into(holder.view.imgMain)

        // pixiv
        val indexPixiv = arrayOf(5, 51)
        if ( jsonfeed.results[position].header.index_id in indexPixiv) {
            holder.view.stTitle.text =  jsonfeed.results[position].data.title
            holder.view.stTinyTitle.text =  jsonfeed.results[position].data.member_name
            holder.view.stSite.text = ctx.getString(R.string.pixiv)
            holder.source = jsonfeed.results[position].data.ext_urls[0]
        }
        // Danbooru 9 / Gelbooru / yande.re 12
        val index3rd = arrayOf(9, 12)
        if ( jsonfeed.results[position].header.index_id in index3rd ) {
            holder.view.stTitle.text = ctx.getString(R.string.noTitle)
            holder.view.stTinyTitle.text = jsonfeed.results[position].data.creator.toString()

            var sourceURL = Uri.parse(jsonfeed.results[position].data.source)
            when (sourceURL.host) {
                "www.pixiv.net" -> holder.view.stSite.text = ctx.getString(R.string.pixiv)
                in pixivImageServer -> holder.view.stSite.text = ctx.getString(R.string.pixiv)
                "twitter.com" -> holder.view.stSite.text = ctx.getString(R.string.twitter)
                else -> holder.view.stSite.text = "nonAdded"
            }
            holder.ext_urls = jsonfeed.results[position].data.ext_urls
            holder.source = jsonfeed.results[position].data.source
        }
        // nico
        if ( jsonfeed.results[position].header.index_id == 8 ) {
            holder.view.stTinyTitle.text =  jsonfeed.results[position].data.member_name
        }

    }

}

class  ViewHolder(val view: View, var source: String? = null, var ext_urls: List<String>? = null): RecyclerView.ViewHolder(view) {
    init {
        view.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)

            if (source != null) {
                val sourceURI = Uri.parse(source)
                when (sourceURI.host) {
                    in pixivImageServer  -> openURL.data = Uri.parse("https://www.pixiv.net/member_illust.php?mode=medium&illust_id=${sourceURI.lastPathSegment?.split("_")?.first()}")
                    else -> openURL.data = sourceURI
                }
                view.context.startActivity(openURL)
            } else {
                Snackbar.make(view,"What?! There's NO source here!", Snackbar.LENGTH_SHORT).show()
            }

        }
    }
}