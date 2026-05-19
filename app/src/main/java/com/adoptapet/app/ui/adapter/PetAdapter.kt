package com.adoptapet.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.adoptapet.app.R
import com.adoptapet.app.data.model.Pet
import com.adoptapet.app.databinding.ItemPetBinding

class PetAdapter(
    private val onPetClick: (Pet) -> Unit,
    private val onAdoptClick: (Pet) -> Unit
) : ListAdapter<Pet, PetAdapter.PetViewHolder>(PetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val binding = ItemPetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PetViewHolder(
        private val binding: ItemPetBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pet: Pet) {
            with(binding) {
                tvPetName.text = pet.name
                tvPetType.text = pet.type.replaceFirstChar { it.uppercase() }
                tvPetAge.text = pet.age
                tvPetDescription.text = pet.description

                // Mejoramos la carga con Coil para URLs externas
                ivPetPhoto.load(pet.photoUrl) {
                    crossfade(true)
                    crossfade(400)
                    // Si la URL está vacía o falla, usa la huella por defecto
                    placeholder(R.drawable.ic_paw_placeholder)
                    error(R.drawable.ic_paw_placeholder)
                    diskCachePolicy(CachePolicy.ENABLED)
                }

                root.setOnClickListener { onPetClick(pet) }
                btnAdopt.setOnClickListener { onAdoptClick(pet) }
            }
        }
    }

    class PetDiffCallback : DiffUtil.ItemCallback<Pet>() {
        override fun areItemsTheSame(oldItem: Pet, newItem: Pet): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Pet, newItem: Pet): Boolean = oldItem == newItem
    }
}