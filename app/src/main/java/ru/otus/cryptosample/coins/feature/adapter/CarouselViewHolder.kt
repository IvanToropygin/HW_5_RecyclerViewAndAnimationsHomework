package ru.otus.cryptosample.coins.feature.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.otus.cryptosample.coins.feature.CoinState
import ru.otus.cryptosample.databinding.ItemCarouselBinding

class CarouselViewHolder(
    binding: ItemCarouselBinding,
    sharedPool: RecyclerView.RecycledViewPool
): RecyclerView.ViewHolder(binding.root) {

    private val carouselAdapter = CarouselAdapter()

    init {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = carouselAdapter
            setRecycledViewPool(sharedPool)
            isNestedScrollingEnabled = false
            setHasFixedSize(true)
            itemAnimator = ItemAnimator()
        }
    }

    fun bind(coins: List<CoinState>) {
        carouselAdapter.submitList(coins)
    }

    fun bind(coins: List<CoinState>, payloads: List<Any>) {
        val carouselPayload = payloads.firstOrNull { it is CoinsAdapter.CoinDiff.CarouselPayload }
                as? CoinsAdapter.CoinDiff.CarouselPayload

        if (carouselPayload != null) {
            carouselAdapter.submitList(coins) {
                carouselPayload.positionChanges.forEach { (position, payload) ->
                    carouselAdapter.notifyItemChanged(position, payload)
                }
            }
        } else {
            carouselAdapter.submitList(coins)
        }
    }
}