package com.rtech.cartly

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.rtech.cartly.data.UserProvider
import com.rtech.cartly.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by activityViewModels()

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var signedOutLayout: LinearLayout
    private lateinit var signedInLayout: LinearLayout
    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var basketCount: TextView
    private lateinit var savingsCount: TextView

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        signedOutLayout = view.findViewById(R.id.signedOutLayout)
        signedInLayout = view.findViewById(R.id.signedInLayout)
        profileName = view.findViewById(R.id.profileName)
        profileEmail = view.findViewById(R.id.profileEmail)
        basketCount = view.findViewById(R.id.basketCount)
        savingsCount = view.findViewById(R.id.savingsCount)

        val btnSignIn = view.findViewById<TextView>(R.id.btnSignIn)
        btnSignIn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            signInLauncher.launch(signInIntent)
        }

        val btnSignOut = view.findViewById<TextView>(R.id.btnSignOut)
        btnSignOut.setOnClickListener {
            auth.signOut()
            googleSignInClient.signOut()
            UserProvider.ensureSignedIn { }
            updateUI()
            viewModel.loadStats(null)
        }

        val btnSettingsBar = view.findViewById<LinearLayout>(R.id.btnSettingsBar)
        btnSettingsBar.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        val btnSettingsBarSignedOut = view.findViewById<LinearLayout>(R.id.btnSettingsBarSignedOut)
        btnSettingsBarSignedOut.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        viewModel.basketCount.observe(viewLifecycleOwner) { count ->
            basketCount.text = "$count items in basket"
        }

        viewModel.favouritesCount.observe(viewLifecycleOwner) { count ->
            savingsCount.text = "$count deals saved"
        }

        updateUI()
        viewModel.loadStats(googleUid())
    }

    override fun onResume() {
        super.onResume()
        updateUI()
        viewModel.loadStats(googleUid())
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val currentUser = auth.currentUser

        val task = if (currentUser != null && currentUser.isAnonymous) {
            currentUser.linkWithCredential(credential)
        } else {
            auth.signInWithCredential(credential)
        }

        task.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Signed in successfully!", Toast.LENGTH_SHORT).show()
                updateUI()
                viewModel.loadStats(googleUid())
            } else {
                Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun googleUid(): String? {
        val user = auth.currentUser ?: return null
        return if (user.isAnonymous) null else user.uid
    }

    private fun updateUI() {
        val user = auth.currentUser
        if (user != null && !user.isAnonymous) {
            signedOutLayout.visibility = View.GONE
            signedInLayout.visibility = View.VISIBLE
            profileName.text = user.displayName ?: "Cartly User"
            profileEmail.text = user.email ?: ""
        } else {
            signedOutLayout.visibility = View.VISIBLE
            signedInLayout.visibility = View.GONE
        }
    }
}