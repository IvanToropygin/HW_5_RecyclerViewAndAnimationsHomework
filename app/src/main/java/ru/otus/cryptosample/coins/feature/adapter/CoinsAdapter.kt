package ru.otus.cryptosample.coins.feature.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.otus.cryptosample.coins.feature.CoinCategoryState
import ru.otus.cryptosample.coins.feature.CoinState
import ru.otus.cryptosample.coins.feature.adapter.CarouselAdapter.Companion.Payload.HOT_MOVER_CHANGED
import ru.otus.cryptosample.databinding.ItemCarouselBinding
import ru.otus.cryptosample.databinding.ItemCategoryHeaderBinding
import ru.otus.cryptosample.databinding.ItemCoinBinding

class CoinsAdapter : ListAdapter<CoinsAdapterItem, RecyclerView.ViewHolder>(CoinDiff) {

    fun setData(categories: List<CoinCategoryState>) {
        val adapterItems = mutableListOf<CoinsAdapterItem>()

        categories.forEach { category ->
            adapterItems.add(CoinsAdapterItem.CategoryHeader(category.name))
            if (category.coins.size <= 10) {
                category.coins.forEach { coin ->
                    adapterItems.add(CoinsAdapterItem.CoinItem(coin))
                }
            } else {
                adapterItems.add(CoinsAdapterItem.Carousel(category.coins))
            }
        }

        submitList(adapterItems)
    }

    override fun getItemCount(): Int = currentList.size

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CoinsAdapterItem.CategoryHeader -> VIEW_TYPE_CATEGORY
            is CoinsAdapterItem.CoinItem -> VIEW_TYPE_COIN
            is CoinsAdapterItem.Carousel -> VIEW_TYPE_CAROUSEL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CATEGORY -> CategoryHeaderViewHolder(
                ItemCategoryHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            VIEW_TYPE_COIN -> {
                val binding =
                    ItemCoinBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                val params = binding.root.layoutParams ?: RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.WRAP_CONTENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
                )
                val screenWidth = parent.resources.displayMetrics.widthPixels
                params.width = screenWidth / 2 - 16

                binding.root.layoutParams = params
                CoinViewHolder(binding)
            }

            VIEW_TYPE_CAROUSEL -> {
                CarouselViewHolder(
                    ItemCarouselBinding
                        .inflate(LayoutInflater.from(parent.context), parent, false),
                    sharedPool
                )
            }

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is CoinsAdapterItem.CategoryHeader -> {
                (holder as CategoryHeaderViewHolder).bind(item.categoryName)
            }

            is CoinsAdapterItem.CoinItem -> {
                (holder as CoinViewHolder).bind(item.coin)
            }

            is CoinsAdapterItem.Carousel -> {
                (holder as CarouselViewHolder).bind(item.coins)
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }

        when (val item = getItem(position)) {
            is CoinsAdapterItem.CoinItem -> {
                (holder as CoinViewHolder).bind(item.coin, payloads)
            }

            is CoinsAdapterItem.Carousel -> {
                (holder as CarouselViewHolder).bind(item.coins, payloads)
            }

            else -> {
                onBindViewHolder(holder, position)
            }
        }
    }

    object CoinDiff : DiffUtil.ItemCallback<CoinsAdapterItem>() {

        override fun areItemsTheSame(
            oldItem: CoinsAdapterItem,
            newItem: CoinsAdapterItem
        ): Boolean =
            when {
                oldItem is CoinsAdapterItem.CategoryHeader &&
                        newItem is CoinsAdapterItem.CategoryHeader ->
                    oldItem.categoryName == newItem.categoryName

                oldItem is CoinsAdapterItem.CoinItem &&
                        newItem is CoinsAdapterItem.CoinItem ->
                    oldItem.coin.id == newItem.coin.id

                oldItem is CoinsAdapterItem.Carousel &&
                        newItem is CoinsAdapterItem.Carousel ->
                    oldItem.coins.firstOrNull()?.id == newItem.coins.firstOrNull()?.id

                else -> false
            }

        override fun areContentsTheSame(
            oldItem: CoinsAdapterItem,
            newItem: CoinsAdapterItem
        ): Boolean = oldItem == newItem

        override fun getChangePayload(oldItem: CoinsAdapterItem, newItem: CoinsAdapterItem): Any? {
            return when (oldItem) {
                is CoinsAdapterItem.CoinItem if newItem is CoinsAdapterItem.CoinItem -> {
                    if (oldItem.coin.highlight != newItem.coin.highlight) {
                        Payload.HOT_MOVER_CHANGED
                    } else {
                        null
                    }
                }

                is CoinsAdapterItem.Carousel if newItem is CoinsAdapterItem.Carousel -> {
                    val changes = findChangesInCarousel(oldItem.coins, newItem.coins)
                    if (changes.isNotEmpty()) {
                        CarouselPayload(changes)
                    } else {
                        null
                    }
                }

                else -> null
            }
        }

        private fun findChangesInCarousel(
            oldList: List<CoinState>,
            newList: List<CoinState>
        ): Map<Int, Any> {
            val changes = mutableMapOf<Int, Any>()
            val maxSize = minOf(oldList.size, newList.size)

            for (i in 0 until maxSize) {
                val oldCoin = oldList[i]
                val newCoin = newList[i]

                if (oldCoin.id == newCoin.id) {
                    if (oldCoin.highlight != newCoin.highlight) {
                        changes[i] = HOT_MOVER_CHANGED
                    }
                }
            }

            return changes
        }

        data class CarouselPayload(val positionChanges: Map<Int, Any>)

        object Payload {
            const val HOT_MOVER_CHANGED = "HOT_MOVER_CHANGED"
        }
    }

    companion object {
        const val VIEW_TYPE_CATEGORY = 0
        const val VIEW_TYPE_COIN = 1
        const val VIEW_TYPE_CAROUSEL = 2

        val sharedPool = RecyclerView.RecycledViewPool().apply {
            setMaxRecycledViews(VIEW_TYPE_COIN, 10)
        }
    }
}

sealed class CoinsAdapterItem {

    data class CategoryHeader(val categoryName: String) : CoinsAdapterItem()
    data class CoinItem(val coin: CoinState) : CoinsAdapterItem()
    data class Carousel(val coins: List<CoinState>) : CoinsAdapterItem()
}