package com.adyen.android.assignment.ui.planetlanding

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adyen.android.assignment.R
import com.adyen.android.assignment.base.BaseActivity
import com.adyen.android.assignment.data.helper.ComplexPreferencesImpl
import com.adyen.android.assignment.data.response.AstronomyResponse
import com.adyen.android.assignment.databinding.ActivityPlanetsLandingBinding
import com.adyen.android.assignment.network.ResultModel
import com.adyen.android.assignment.ui.planetdetails.PlanetDetailsActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class PlanetsLandingActivity : BaseActivity<ActivityPlanetsLandingBinding>() {
    private val TAG = "PlanetsLandingActivity"

    private val viewModel: PlanetsLandingViewModel by viewModels()

    private lateinit var planetsAdapter: PlanetsAdapter

    private lateinit var planetsFavouriteAdapter: PlanetsAdapter

    private lateinit var orderOptions: Array<String>

    private lateinit var dialog: AlertDialog

    override fun getViewBinding() = ActivityPlanetsLandingBinding.inflate(layoutInflater)

    private lateinit var complexPreferences: ComplexPreferencesImpl

    override fun setupView() {

        complexPreferences = ComplexPreferencesImpl(this)
        viewModel.setComplexPref(complexPreferences)
        handleFavouriteState()


        setupViewModelObservers()
        initListeners()
        fillData()
    } // fun of setupView

    override fun onResume() {
        super.onResume()
        handleFavouriteState()
    }

    private fun handleFavouriteState() {
        viewModel.handleIfAlreadyFavourite()
    }


    private fun fillData() {
        // adapter
        initPlanetList()
    }

    private fun initListeners() {
        // handle Fab button
        binding.extendedFab.setOnClickListener {
            showOrderDialog()
        }

        // handle error refresh click
        binding.layoutError.button.setOnClickListener {
            viewModel.refresh()
            hideErrorAndRefresh()
        }


    } // fun of initListeners

    private fun showOrderDialog() {
        orderOptions =
            arrayOf(getString(R.string.reorder_by_title), getString(R.string.reorder_by_date))

        val layoutInflater = LayoutInflater.from(this)
        val promptView: View = layoutInflater.inflate(
            com.adyen.android.assignment.R.layout.custom_reorder_dialog,
            null
        )
        val alertD = MaterialAlertDialogBuilder(this)

        val btn_apply: Button =
            promptView.findViewById<View>(com.adyen.android.assignment.R.id.btn_apply) as Button
        val btn_reset: Button =
            promptView.findViewById<View>(com.adyen.android.assignment.R.id.btn_reset) as Button

        val btn_orderTitle = promptView.findViewById<View>(R.id.radio_order_title) as RadioButton
        val btn_orderDate = promptView.findViewById<View>(R.id.radio_order_date) as RadioButton

        btn_orderTitle.setOnClickListener {
            viewModel.selectedOrderIndex = 0
        }

        btn_orderDate.setOnClickListener {
            viewModel.selectedOrderIndex = 1
        }

        btn_apply.setOnClickListener {
            viewModel.sortList(viewModel.selectedOrderIndex)
            dialog.dismiss()
        }

        btn_reset.setOnClickListener {
            viewModel.resetSort()
            dialog.dismiss()
        }

        alertD.setView(promptView)
        dialog = alertD.show()
    }

    private fun hideErrorAndRefresh() {
        handleError(isError = false)
    }

    private fun handlePlanetSelection(planet: AstronomyResponse?) {
        val myIntent = Intent(this, PlanetDetailsActivity::class.java)
        myIntent.putExtra("planetDetails", planet)
        startActivity(myIntent)
    }

    var planetsOnClickListener = (PlanetsAdapter.OnClickListener { planet ->
        handlePlanetSelection(planet)
    })

    private fun initPlanetList() {
        planetsAdapter = PlanetsAdapter(
            context = this@PlanetsLandingActivity,
            onClickListener = planetsOnClickListener
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = planetsAdapter
    } // fun of initPlanetList

    override fun setupViewModelObservers() {
        viewModel.planetsDataObserver.observe(this, planetsDataObserver)

        viewModel.planetsDataObserverFav .observe(this) {
            handleFavouriteIconState(it)
        }

    } // fun of setupViewModelObservers

    private fun handleProgress(isLoading: Boolean) {
        if (isLoading)
            binding.progressBar.visibility = View.VISIBLE
        else
            binding.progressBar.visibility = View.GONE
    } // fun of handleProgress

    private fun onFail() {
        handleProgress(isLoading = false)
        handleError(isError = true)
    } // fun of onFail*/

    private fun handleError(isError: Boolean) {
        if (isError) {
            binding.layoutError.layoutError.visibility = View.VISIBLE
            binding.extendedFab.visibility = View.GONE
        } else {
            binding.layoutError.layoutError.visibility = View.GONE
            binding.extendedFab.visibility = View.VISIBLE
        }
    }

    var planetsFavOnClickListener = (PlanetsAdapter.OnClickListener { planet ->
        handlePlanetSelection(planet)
    })

    private fun handleFavouriteIconState(list: ArrayList<AstronomyResponse>){
        if (list.isNotEmpty()){
            binding.txtMyfavourites.visibility = View.VISIBLE
            binding.rcvFavourites.visibility = View.VISIBLE

            planetsFavouriteAdapter = PlanetsAdapter(
                planetList = list,
                context = this@PlanetsLandingActivity,
                planetsFavOnClickListener
            )
            binding.rcvFavourites.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            binding.rcvFavourites.adapter = planetsFavouriteAdapter

            planetsFavouriteAdapter.notifyDataSetChanged()

        }else{
            binding.txtMyfavourites.visibility = View.GONE
            binding.rcvFavourites.visibility = View.GONE
        }
    }


    private val planetsDataObserver = Observer<ResultModel<List<AstronomyResponse>>> { result ->
        lifecycleScope.launch {
            when (result) {
                is ResultModel.Loading -> {
                    handleProgress(isLoading = result.isLoading ?: false)
                } // Loading
                is ResultModel.Success -> {
                    onSuccess(data = result.data)
                } // Success
                is ResultModel.Failure -> {
                    onFail()
                } // Fail
            }
        }
    } // weatherDataObserver

    private fun onSuccess(data: List<AstronomyResponse>?) {
        handleProgress(isLoading = false)
        planetsAdapter.submitList((data ?: arrayListOf()))
    } // fun of onSuccess
}
