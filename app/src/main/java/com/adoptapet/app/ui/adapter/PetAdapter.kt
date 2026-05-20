/**
 * Adaptador del RecyclerView para mostrar las mascotas disponibles en adopción.
 * Cada elemento permite ver la información de la mascota y realizar acciones
 * como abrir el detalle o iniciar el proceso de adopción.
 *
 * @param onPetClick Función que se ejecuta al seleccionar una mascota.
 * @param onAdoptClick Función que se ejecuta al presionar el botón "Adoptar".
 */
class PetAdapter(
    private val onPetClick: (Pet) -> Unit,
    private val onAdoptClick: (Pet) -> Unit
) : ListAdapter<Pet, PetAdapter.PetViewHolder>(PetDiffCallback()) {

    /**
     * Crea la vista de cada elemento usando el layout item_pet.xml.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val binding = ItemPetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PetViewHolder(binding)
    }

    /**
     * Asocia la mascota correspondiente con la vista del ítem.
     */
    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder que administra los componentes visuales de cada mascota.
     */
    inner class PetViewHolder(
        private val binding: ItemPetBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Carga los datos de la mascota en la interfaz.
         */
        fun bind(pet: Pet) {
            with(binding) {

                // Muestra el nombre de la mascota.
                tvPetName.text = pet.name

                // Muestra el tipo con la primera letra en mayúscula.
                tvPetType.text = pet.type.replaceFirstChar { it.uppercase() }

                // Muestra la edad de la mascota.
                tvPetAge.text = pet.age

                // Muestra la descripción de la mascota.
                tvPetDescription.text = pet.description

                // Carga la foto de la mascota desde la URL.
                ivPetPhoto.load(pet.photoUrl) {

                    // Aplica una transición suave.
                    crossfade(true)

                    // Define la duración de la transición.
                    crossfade(400)

                    // Imagen temporal mientras carga.
                    placeholder(R.drawable.ic_paw_placeholder)

                    // Imagen por defecto si ocurre un error.
                    error(R.drawable.ic_paw_placeholder)

                    // Guarda la imagen en caché local.
                    diskCachePolicy(CachePolicy.ENABLED)
                }

                // Abre el detalle al seleccionar la tarjeta.
                root.setOnClickListener {
                    onPetClick(pet)
                }

                // Inicia el proceso de adopción al presionar el botón.
                btnAdopt.setOnClickListener {
                    onAdoptClick(pet)
                }
            }
        }
    }

    /**
     * Compara elementos de la lista para actualizar solo los cambios necesarios.
     */
    class PetDiffCallback : DiffUtil.ItemCallback<Pet>() {

        /**
         * Verifica si ambas mascotas tienen el mismo ID.
         */
        override fun areItemsTheSame(oldItem: Pet, newItem: Pet): Boolean =
            oldItem.id == newItem.id

        /**
         * Verifica si el contenido de la mascota cambió.
         */
        override fun areContentsTheSame(oldItem: Pet, newItem: Pet): Boolean =
            oldItem == newItem
    }
}