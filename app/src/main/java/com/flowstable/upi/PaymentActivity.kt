package com.flowstable.upi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flowstable.upi.ussd.USSDController
import com.flowstable.upi.ussd.UPIData
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class PaymentActivity : AppCompatActivity() {

    private lateinit var etUpiId: TextInputEditText
    private lateinit var etName: TextInputEditText
    private lateinit var etAmount: TextInputEditText
    private lateinit var btnPay: MaterialButton

    // UPI ID format: localpart@bankhandle
    // localpart: alphanumeric, dots, hyphens, underscores (3–256 chars)
    // bankhandle: letters only, 2–64 chars
    private val upiIdRegex = Regex("^[a-zA-Z0-9._\\-]{3,256}@[a-zA-Z][a-zA-Z0-9]{1,64}$")

    // Amount: positive number, max 2 decimal places, no more than 1,00,000
    private val maxTransactionAmount = 100000.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        initViews()
        parseIntentData()
        setupClickListeners()
    }

    private fun initViews() {
        etUpiId = findViewById(R.id.etUpiId)
        etName = findViewById(R.id.etName)
        etAmount = findViewById(R.id.etAmount)
        btnPay = findViewById(R.id.btnPay)
    }

    private fun parseIntentData() {
        val upiString = intent.getStringExtra("upi_string") ?: return

        try {
            val uri = Uri.parse(upiString)

            // Validate it's actually a UPI URI scheme
            if (uri.scheme?.lowercase() != "upi") {
                Toast.makeText(this, "Invalid QR: Not a UPI code", Toast.LENGTH_SHORT).show()
                return
            }

            val pa = uri.getQueryParameter("pa") // Payee address (UPI ID)
            val pn = uri.getQueryParameter("pn") // Payee name
            val am = uri.getQueryParameter("am") // Amount

            // Validate the UPI ID from QR before filling the field
            if (pa != null && upiIdRegex.matches(pa)) {
                etUpiId.setText(pa)
            } else if (pa != null) {
                Toast.makeText(this, "QR contains an invalid UPI ID", Toast.LENGTH_SHORT).show()
            }

            pn?.let { etName.setText(it.take(100)) } // Cap name length
            am?.let {
                val parsedAmount = it.toDoubleOrNull()
                if (parsedAmount != null && parsedAmount > 0 && parsedAmount <= maxTransactionAmount) {
                    etAmount.setText(it)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid QR code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        btnPay.setOnClickListener {
            if (validateAndPay()) {
                // validateAndPay handles everything internally
            }
        }

        findViewById<android.widget.ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun validateAndPay(): Boolean {
        val upiId = etUpiId.text?.toString()?.trim() ?: ""
        val name = etName.text?.toString()?.trim() ?: ""
        val amount = etAmount.text?.toString()?.trim() ?: ""

        // --- UPI ID Validation ---
        if (upiId.isEmpty()) {
            etUpiId.error = "Enter UPI ID"
            etUpiId.requestFocus()
            return false
        }

        if (!upiIdRegex.matches(upiId)) {
            etUpiId.error = "Invalid UPI ID format (e.g. name@upi)"
            etUpiId.requestFocus()
            return false
        }

        // --- Amount Validation ---
        if (amount.isEmpty()) {
            etAmount.error = "Enter amount"
            etAmount.requestFocus()
            return false
        }

        val amountDouble = amount.toDoubleOrNull()
        if (amountDouble == null || amountDouble <= 0) {
            etAmount.error = "Enter a valid positive amount"
            etAmount.requestFocus()
            return false
        }

        if (amountDouble > maxTransactionAmount) {
            etAmount.error = "Amount cannot exceed ₹1,00,000 per transaction"
            etAmount.requestFocus()
            return false
        }

        // Check for more than 2 decimal places
        val decimalIndex = amount.indexOf('.')
        if (decimalIndex != -1 && amount.length - decimalIndex - 1 > 2) {
            etAmount.error = "Amount can have at most 2 decimal places"
            etAmount.requestFocus()
            return false
        }

        // --- All good, initiate payment ---
        val upiData = UPIData(
            upiId = upiId,
            name = name,
            amount = amount
        )

        USSDController.currentPayment = upiData
        USSDController.currentFlow = USSDController.Flow.PAYMENT
        initiateUSSDPayment()
        return true
    }

    private fun initiateUSSDPayment() {
        try {
            val ussdCode = "*99${Uri.encode("#")}"
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$ussdCode")
            startActivity(intent)

            val processingIntent = Intent(this, ProcessingActivity::class.java)
            startActivity(processingIntent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to initiate payment: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
