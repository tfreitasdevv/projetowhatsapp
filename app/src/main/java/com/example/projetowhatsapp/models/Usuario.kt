package com.example.projetowhatsapp.models

data class Usuario(
    var id: String,
    var nome: String,
    var email: String,
    var foto: String = ""
)
