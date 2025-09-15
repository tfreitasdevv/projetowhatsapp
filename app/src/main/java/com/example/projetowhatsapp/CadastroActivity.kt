package com.example.projetowhatsapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.projetowhatsapp.databinding.ActivityCadastroBinding
import com.example.projetowhatsapp.databinding.ActivityLoginBinding
import com.example.projetowhatsapp.models.Usuario
import com.example.projetowhatsapp.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore


class CadastroActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityCadastroBinding.inflate(layoutInflater)
    }

    private lateinit var nome: String
    private lateinit var email: String
    private lateinit var senha: String

    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Use a view raiz do seu layout ou outra view válida
        val rootView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        inicializarToolbar()
        inicializarEventosClique()
    }

    private fun inicializarEventosClique() {
        binding.btnCadastrar.setOnClickListener {
            if (validarCampos()) {
                cadastrarUsuario(nome, email, senha)
            }
        }
    }

    private fun cadastrarUsuario(nome: String, email: String, senha: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { resultado ->
                if (resultado.isSuccessful) {

                    // Salvar dados do usuário no Firestore
                    val idUsuario = resultado.result.user?.uid
                    if (idUsuario != null) {
                        val usuario = Usuario(
                            idUsuario, nome, email
                        )
                        salvarUsuarioFirestore(usuario)
                    }
                }
            }.addOnFailureListener { erro ->
               try {
                   throw erro
               } catch (erroCredenciaisInvalidas: FirebaseAuthInvalidCredentialsException) {
                   erroCredenciaisInvalidas.printStackTrace()
                   exibirMensagem("E-mail inválido!")
               } catch (erroUsuarioExistente: FirebaseAuthUserCollisionException) {
                   erroUsuarioExistente.printStackTrace()
                   exibirMensagem("Este e-mail já está em uso!")
               } catch (erroSenhaFraca: FirebaseAuthWeakPasswordException) {
                   erroSenhaFraca.printStackTrace()
                   exibirMensagem("Senha fraca!")
               }
            }
    }

    private fun salvarUsuarioFirestore(usuario: Usuario) {
        firestore.collection("usuarios")
            .document(usuario.id)
            .set(usuario)
            .addOnSuccessListener {
                exibirMensagem("Cadastro realizado com sucesso!")
                startActivity(
                    Intent(applicationContext, MainActivity::class.java)
                )
            }
            .addOnFailureListener { erro ->
                exibirMensagem("Erro ao salvar usuário: ${erro.message}")
            }
    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeToolbar.tbPrincipal
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Faça o seu cadastro"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun validarCampos(): Boolean {
        nome = binding.editNome.text.toString().trim()
        email = binding.editEmail.text.toString().trim()
        senha = binding.editSenha.text.toString().trim()

        if (nome.isNotEmpty()) {
            binding.textInputLayoutNome.error = null
            if (email.isNotEmpty()) {
                binding.textInputLayoutEmail.error = null
                if (senha.isNotEmpty()) {
                    binding.textInputLayoutSenha.error = null
                    return true
                } else {
                    binding.textInputLayoutSenha.error = "Preencha a senha"
                    return false
                }
            } else {
                binding.textInputLayoutEmail.error = "Preencha o e-mail"
                return false
            }
        } else {
            binding.textInputLayoutNome.error = "Preencha o nome"
            return false
        }
    }
}

