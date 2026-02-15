package ru.otus.cryptosample.coins.feature.adapter

import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class ItemAnimator : DefaultItemAnimator() {

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        val view: View = holder.itemView
        addDuration = when (holder) {
            is CoinViewHolder -> 400L
            is CategoryHeaderViewHolder -> 50L
            else -> 200L
        }

        view.alpha = 0f
        view.translationX = view.width * -1f
        view.scaleX = 0.9f
        view.scaleY = 0.9f

        val animator = view.animate()
            .alpha(1f)
            .translationX(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(addDuration)
            .setListener(null)

        animator.withStartAction {
            dispatchAddStarting(holder)
        }

        animator.withEndAction {
            view.alpha = 1f
            view.translationX = 0f
            view.scaleX = 1f
            view.scaleY = 1f
            dispatchAddFinished(holder)
            view.animate().setListener(null)
        }

        animator.start()
        return true
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        holder.itemView.animate().cancel()

        val view = holder.itemView
        view.alpha = 1f
        view.translationX = 0f
        view.scaleX = 1f
        view.scaleY = 1f

        val animator = view.animate()
            .alpha(0f)
            .translationX(view.width * -1f)
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(removeDuration)
            .setListener(null)

        animator.withStartAction {
            dispatchRemoveStarting(holder)
        }

        animator.withEndAction {
            view.alpha = 1f
            view.translationX = 0f
            view.scaleX = 1f
            view.scaleY = 1f
            dispatchRemoveFinished(holder)
        }

        animator.start()
        return true
    }

    override fun runPendingAnimations() {
        super.runPendingAnimations()
    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        item.itemView.animate().cancel()
        super.endAnimation(item)
    }

    override fun endAnimations() {
        super.endAnimations()
    }
}