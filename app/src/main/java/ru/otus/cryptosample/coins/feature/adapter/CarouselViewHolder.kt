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
        val carouselPayloads = payloads.filterIsInstance<CoinsAdapter.CoinDiff.CarouselPayload>()

        if (carouselPayloads.isNotEmpty()) {
            val allChanges = carouselPayloads
                .flatMap { it.positionChanges.entries }
                .associate { it.key to it.value }

            carouselAdapter.submitList(coins) {
                allChanges.forEach { (position, payload) ->
                    carouselAdapter.notifyItemChanged(position, payload)
                }
            }
        } else {
            carouselAdapter.submitList(coins)
        }
    }
}