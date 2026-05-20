// app/src/main/java/com/adoptapet/app/ui/adapter/MyPetAdapter.kt
// Adapter del RecyclerView para las mascotas propias en la pantalla de perfil

package com.adoptapet.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.adoptapet.app.R
import com.adoptapet.app.data.model.Pet
import com.adoptapet.app.databinding.ItemMyPetBinding

/**
 * Adapter para la lista de mascotas propias en la pantalla de Perfil.
 * Muestra una vista compacta con botón de eliminación.
 *
 * @param onDeleteClick  Callback al presionar "Eliminar" en un ítem
 */
class MyPetAdapter(
    private val onDeleteClick: (Pet) -> Unit
) : ListAdapter<Pet, MyPetAdapter.MyPetViewHolder>(MyPetDiffCallback()) {
    /**
     * Crea la vista de cada elemento usando el layout item_my_pet.xml.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPetViewHolder {
        val binding = ItemMyPetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyPetViewHolder(binding)
    }
    /**
     * ViewHolder que administra los componentes visuales de cada mascota.
     */
    override fun onBindViewHolder(holder: MyPetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MyPetViewHolder(
        private val binding: ItemMyPetBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        /**
         * Carga los datos de la mascota en la interfaz.
         */
        fun bind(pet: Pet) {
            with(binding) {
                tvMyPetName.text = pet.name
                tvMyPetType.text = pet.type.replaceFirstChar { it.uppercase() }
                tvMyPetAge.text = pet.age

                // Cargar miniatura con Coil
                if (pet.photoUrl.isNotEmpty()) {
                    ivMyPetPhoto.load(pet.photoUrl) {
                        crossfade(true)
                        placeholder(R.drawable.ic_paw_placeholder)
                        error(R.drawable.ic_paw_placeholder)
                        transformations(RoundedCornersTransformation(12f))
                    }
                } else {
                    ivMyPetPhoto.setImageResource(R.drawable.ic_paw_placeholder)
                }

                // Botón eliminar
                btnDeletePet.setOnClickListener { onDeleteClick(pet) }
            }
        }
    }
    /**
     * Compara elementos de la lista para actualizar solo los cambios necesarios.
     */
    class MyPetDiffCallback : DiffUtil.ItemCallback<Pet>() {
        override fun areItemsTheSame(oldItem: Pet, newItem: Pet) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Pet, newItem: Pet) = oldItem == newItem
    }
}
