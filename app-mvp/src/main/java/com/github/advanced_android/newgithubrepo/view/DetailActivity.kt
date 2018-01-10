package com.github.advanced_android.newgithubrepo.view

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.graphics.drawable.RoundedBitmapDrawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.github.advanced_android.newgithubrepo.R
import com.github.advanced_android.newgithubrepo.contract.DetailContract
import com.github.advanced_android.newgithubrepo.model.GitHubService
import com.github.advanced_android.newgithubrepo.presenter.DetailPresenter

/**
 * 詳細画面を表示するActivity
 */
class DetailActivity : AppCompatActivity(), DetailContract.View {
    private var fullNameTextView: TextView? = null
    private var detailTextView: TextView? = null
    private var repoStarTextView: TextView? = null
    private var repoForkTextView: TextView? = null
    private var ownerImage: ImageView? = null
    private var detailPresenter: DetailContract.UserActions? = null
    private var fullRepoName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        val intent = intent
        fullRepoName = intent.getStringExtra(EXTRA_FULL_REPOSITORY_NAME)

        fullNameTextView = this@DetailActivity.findViewById(R.id.fullname) as TextView?
        detailTextView = findViewById(R.id.detail) as TextView?
        repoStarTextView = findViewById(R.id.repo_star) as TextView?
        repoForkTextView = findViewById(R.id.repo_fork) as TextView?
        ownerImage = findViewById(R.id.owner_image) as ImageView?


        val gitHubService = (application as NewGitHubReposApplication).gitHubService
        detailPresenter = DetailPresenter(this as DetailContract.View, gitHubService)
        detailPresenter!!.prepare()
    }

    override fun getFullRepositoryName(): String? {
        return fullRepoName
    }

    override fun showRepositoryInfo(response: GitHubService.RepositoryItem) {
        fullNameTextView!!.text = response.full_name
        detailTextView!!.text = response.description
        repoStarTextView!!.text = response.stargazers_count
        repoForkTextView!!.text = response.forks_count
        // サーバーから画像を取得してimageViewに入れる
        Glide.with(this@DetailActivity)
                .load(response.owner.avatar_url)
                .asBitmap().centerCrop().into(object : BitmapImageViewTarget(ownerImage!!) {
            override fun setResource(resource: Bitmap) {
                val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, resource)
                circularBitmapDrawable.isCircular = true
                ownerImage!!.setImageDrawable(circularBitmapDrawable)
            }
        })
        // ロゴとリポジトリの名前をタップしたら、作者のGitHubのページをブラウザで開く
        val listener = View.OnClickListener { detailPresenter!!.titleClick() }
        fullNameTextView!!.setOnClickListener(listener)
        ownerImage!!.setOnClickListener(listener)
    }

    /**
     * @throws Exception
     */
    override fun startBrowser(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override fun showError(message: String) {
        Snackbar.make(findViewById(android.R.id.content)!!, message, Snackbar.LENGTH_LONG)
                .show()
    }

    companion object {
        private val EXTRA_FULL_REPOSITORY_NAME = "EXTRA_FULL_REPOSITORY_NAME"

        /**
         * DetailActivityを開始するメソッド
         * @param fullRepositoryName 表示したいリポジトリの名前(google/ioschedなど)
         */
        fun start(context: Context, fullRepositoryName: String) {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra(EXTRA_FULL_REPOSITORY_NAME, fullRepositoryName)
            context.startActivity(intent)
        }
    }

}
