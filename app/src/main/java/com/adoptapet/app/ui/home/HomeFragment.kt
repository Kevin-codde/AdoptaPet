// app/src/main/java/com/adoptapet/app/ui/home/HomeFragment.kt
// Fragment de inicio: lista de mascotas con búsqueda

package com.adoptapet.app.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.adoptapet.app.data.local.AppDatabase
import com.adoptapet.app.data.model.Pet
import com.adoptapet.app.data.repository.PetRepository
import com.adoptapet.app.databinding.FragmentHomeBinding
import com.adoptapet.app.ui.adapter.PetAdapter
import com.adoptapet.app.ui.contracts.HomeContract
import com.adoptapet.app.ui.presenter.HomePresenter

/**
 * Fragment de la pantalla de inicio.
 * Implementa [HomeContract.View] y delega la lógica al [HomePresenter].
 *
 * Muestra:
 * - AppBar con título y ícono de huella
 * - SearchBar para filtrado en tiempo real
 * - RecyclerView con tarjetas de mascotas disponibles
 * - Estado de carga y estado vacío
 */
class HomeFragment : Fragment(), HomeContract.View {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var presenter: HomePresenter
    private lateinit var petAdapter: PetAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPresenter()
        setupRecyclerView()
        setupSearchBar()

        // Iniciar carga de mascotas
        presenter.loadPets()
    }

    /**
     * Inicializa el Presenter con el repositorio necesario.
     */
    private fun setupPresenter() {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = PetRepository(database.petDao())
        presenter = HomePresenter(this, repository)
    }

    /**
     * Configura el RecyclerView con su adapter y layout manager.
     */
    private fun setupRecyclerView() {
        petAdapter = PetAdapter(
            onPetClick = { pet -> presenter.onPetSelected(pet) },
            onAdoptClick = { pet -> presenter.onPetSelected(pet) }
        )

        binding.rvPets.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = petAdapter
            setHasFixedSize(false)
        }
    }

    /**
     * Configura el SearchBar para búsqueda en tiempo real con debounce básico.
     */
    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                presenter.searchPets(s?.toString() ?: "")
            }
        })
    }

    // ─── HomeContract.View ─────────────────────────────────────────────────

    override fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showPets(pets: List<Pet>) {
        petAdapter.submitList(pets)
    }

    override fun showEmptyState(show: Boolean) {
        binding.emptyState.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvPets.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun navigateToDetail(pet: Pet) {
        // Safe Args genera HomeFragmentDirections desde el nav_graph
        val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(pet.id)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
        _binding = null
    }
}
