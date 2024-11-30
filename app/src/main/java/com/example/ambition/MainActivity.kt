package com.example.ambition

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ambition.viewmodel.AIViewModel
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    // Lazy initialization of AIViewModel
    private val aiViewModel: AIViewModel by viewModels()

    // Add your API key here
    private val apiKey = "your-api-key"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etQuestion = findViewById<TextInputEditText>(R.id.etQuestion)
        val txtResponse = findViewById<TextView>(R.id.txtResponse)
        val idTVQuestion = findViewById<TextView>(R.id.idTVQuestion)

        // Observe loading state
        lifecycleScope.launch {
            aiViewModel.loading.collectLatest { isLoading ->
                if (isLoading) {
                    txtResponse.text = getString(R.string.please_wait)
                }
            }
        }

        // Observe AI response
        lifecycleScope.launch {
            aiViewModel.response.collectLatest { response ->
                txtResponse.text = response
            }
        }

        etQuestion.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                val question = etQuestion.text.toString().trim()
                if (question.isNotEmpty()) {
                    idTVQuestion.text = question
                    etQuestion.text?.clear()
                    aiViewModel.getResponse(question, apiKey)
                } else {
                    Toast.makeText(this, getString(R.string.enter_question), Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }
    }
}
