package com.adyen.android.assignment.ui.planetdetails

import android.graphics.drawable.Drawable
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adyen.android.assignment.R
import com.adyen.android.assignment.base.BaseActivity
import com.adyen.android.assignment.data.helper.ComplexPreferencesImpl
import com.adyen.android.assignment.data.response.AstronomyResponse
import com.adyen.android.assignment.databinding.ActivityPlanetDetailsBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class PlanetDetailsActivity : BaseActivity<ActivityPlanetDetailsBinding>() {
    private val TAG = "PlanetDetailsActivity"

    private val viewModel: PlanetsDetailsViewModel by viewModels()

    private lateinit var planetAttributesAdapter: PlanetAttributesAdapter

    override fun getViewBinding() = ActivityPlanetDetailsBinding.inflate(layoutInflater)

    lateinit var astronomyResponse: AstronomyResponse

    private lateinit var complexPreferences: ComplexPreferencesImpl

    override fun setupView() {
        complexPreferences = ComplexPreferencesImpl(this)
        viewModel.setComplexPref(complexPreferences)

        setupViewModelObservers()
        initListeners()
        getData()
        setupToolBar()
        fillData()
        handleFavouriteState()

    } // fun of setupView

    private fun handleFavouriteState() {
      viewModel.handleIfAlreadyFavourite()
    }

    private fun initListeners() {
        binding.imgFavourite.setOnClickListener {
          viewModel.handleFavClicked( !viewModel.isFavPlanet)
        }
    }

    private fun handleFavouriteIconState(show:Boolean){
        if (show)
            Glide.with(this)
                .load(AppCompatResources.getDrawable(this, R.drawable.ic_favorite_filled))
                .into(binding.imgFavourite)
        else
            Glide.with(this)
                .load(AppCompatResources.getDrawable(this, R.drawable.ic_favorite_border))
                .into(binding.imgFavourite)
    }

    private fun setupToolBar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun getData() {
        val value = intent.extras?.get("planetDetails")
        astronomyResponse = value as AstronomyResponse
       viewModel.setPlanetData(astronomyResponse)
    }

    private fun preparePlanetFeatures(): ArrayList<Pair<String, Drawable>> {
        val list = ArrayList<Pair<String, Drawable>>()
        list.add(Pair(getString(R.string.wrapped_text), resources.getDrawable(R.drawable.ic_warped)))
        list.add(Pair(getString(R.string.billion_star_text), resources.getDrawable(R.drawable.ic_stars)))
        list.add(Pair(getString(R.string.dusty_text), resources.getDrawable(R.drawable.ic_dust)))
        list.add(Pair(getString(R.string.black_hole_text), resources.getDrawable(R.drawable.ic_blackhole)))
        return list
    }

    private fun fillData() {
        binding.txtPlanetTitle.text = astronomyResponse.title
        binding.txtDate.text = astronomyResponse.date
        binding.txtDescription.text = astronomyResponse.explanation

        if (astronomyResponse.hdurl.isNullOrEmpty())
            astronomyResponse.hdurl = astronomyResponse.url

        if (astronomyResponse.media_type != "video")
            Glide.with(this)
                .applyDefaultRequestOptions(
                    RequestOptions()
                        .diskCacheStrategy(
                            DiskCacheStrategy.AUTOMATIC
                        )
                )
                .load(astronomyResponse.hdurl)
                .into(binding.imgPlanetBg)
        else {
            Glide.with(this)
                .load(AppCompatResources.getDrawable(this, R.color.on_background))
                .into(binding.imgPlanetBg)
        }

        // adapter
        initPlanetList()
    }

    private fun initPlanetList() {
        planetAttributesAdapter =
            PlanetAttributesAdapter(preparePlanetFeatures(), this@PlanetDetailsActivity)
        binding.recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = planetAttributesAdapter
    } // fun of initPlanetList

    override fun setupViewModelObservers() {
   viewModel.planetsDataObserverFav .observe(this) {
            handleFavouriteIconState(it)
        }
    }
}
