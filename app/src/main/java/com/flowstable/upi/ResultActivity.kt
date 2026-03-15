package com.flowstable.upi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.flowstable.upi.ussd.USSDController
import com.google.android.material.button.MaterialButton

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val isSuccess   = intent.getBooleanExtra("is_success", false)
        val resultType  = intent.getStringExtra("result_type") ?: "payment"   // "payment" | "balance"
        val customMsg   = intent.getStringExtra("message")

        // Snapshot the payment BEFORE anything can reset it
        val payment = USSDController.currentPayment

        val ivStatus  = findViewById<ImageView>(R.id.ivStatus)
        val tvTitle   = findViewById<TextView>(R.id.tvTitle)
        val tvMessage = findViewById<TextView>(R.id.tvMessage)
        val tvAmount  = findViewById<TextView>(R.id.tvAmount)
        val btnDone   = findViewById<MaterialButton>(R.id.btnDone)

        when {

            // ── Balance result ──────────────────────────────────────────────
            resultType == "balance" -> {
                ivStatus.setImageResource(R.drawable.ic_success)
                ivStatus.setColorFilter(ContextCompat.getColor(this, R.color.success))
                tvTitle.text   = "Balance Retrieved"
                tvMessage.text = customMsg ?: "Balance check complete"
                tvAmount.visibility = View.GONE          // no ₹0 on balance screen
            }

            // ── Payment success ─────────────────────────────────────────────
            isSuccess -> {
                ivStatus.setImageResource(R.drawable.ic_success)
                ivStatus.setColorFilter(ContextCompat.getColor(this, R.color.success))
                tvTitle.text = "Payment Successful"

                // Show who we paid
                val recipient = payment?.name?.ifEmpty { payment.upiId } ?: "Recipient"
                tvMessage.text = customMsg ?: "Sent to $recipient"

                // Show the actual amount — never fall back to 0 silently
                val amount = payment?.amount?.takeIf { it.isNotBlank() && it != "0" }
                if (amount != null) {
                    tvAmount.text       = "₹$amount"
                    tvAmount.visibility = View.VISIBLE
                } else {
                    tvAmount.visibility = View.GONE
                }
            }

            // ── Payment failed ──────────────────────────────────────────────
            else -> {
                ivStatus.setImageResource(R.drawable.ic_error)
                ivStatus.setColorFilter(ContextCompat.getColor(this, R.color.error))
                tvTitle.text        = "Payment Failed"
                tvMessage.text      = customMsg ?: "Please try again"
                tvAmount.visibility = View.GONE
            }
        }

        btnDone.setOnClickListener {
            USSDController.reset()
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}
