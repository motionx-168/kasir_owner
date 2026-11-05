package com.digitalwhy.kasir

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

class ProductListActivity : AppCompatActivity() {

    private lateinit var rvProducts: RecyclerView
    private lateinit var fabAddProduct: FloatingActionButton
    private val adapter = ProductAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_product_list)

            rvProducts = findViewById(R.id.rvProducts)
            rvProducts.layoutManager = LinearLayoutManager(this)
            rvProducts.adapter = adapter

            fabAddProduct = findViewById(R.id.fabAddProduct)
            fabAddProduct.setOnClickListener { showAddProductDialog() }

            loadProducts()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun showAddProductDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_product, null)
        val etName = view.findViewById<EditText>(R.id.etName)
        val etBarcode = view.findViewById<EditText>(R.id.etBarcode)
        val etCostPrice = view.findViewById<EditText>(R.id.etCostPrice)
        val etPrice = view.findViewById<EditText>(R.id.etPrice)
        val etStock = view.findViewById<EditText>(R.id.etStock)
        val spType = view.findViewById<Spinner>(R.id.spType)
        val spIcon = view.findViewById<Spinner>(R.id.spIcon)

        val typeLabels = arrayOf("Barcode", "Timbangan (kg)", "Satuan (Bungkus)")
        spType.adapter = ArrayAdapter(this, R.layout.item_spinner, typeLabels).apply {
            setDropDownViewResource(R.layout.item_spinner)
        }

        val iconLabels = arrayOf("Package", "Egg", "Milk", "Wheat")
        spIcon.adapter = ArrayAdapter(this, R.layout.item_spinner, iconLabels).apply {
            setDropDownViewResource(R.layout.item_spinner)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        view.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                etName.error = "Nama produk wajib diisi"
                return@setOnClickListener
            }

            val barcode = etBarcode.text.toString().trim().ifEmpty {
                "MANUAL-" + UUID.randomUUID().toString().substring(0, 8).uppercase(Locale.getDefault())
            }

            val product = Product(
                id = "0",
                barcode = barcode,
                name = name,
                price = etPrice.text.toString().toDoubleOrNull() ?: 0.0,
                stock = etStock.text.toString().toDoubleOrNull() ?: 0.0,
                costPrice = etCostPrice.text.toString().toDoubleOrNull() ?: 0.0,
                type = when (spType.selectedItemPosition) {
                    1 -> "weight"
                    2 -> "unit"
                    else -> "barcode"
                },
                icon = when (spIcon.selectedItemPosition) {
                    1 -> "egg"
                    2 -> "milk"
                    3 -> "wheat"
                    else -> "package"
                }
            )

            saveProduct(product, dialog)
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun saveProduct(product: Product, dialog: AlertDialog) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.addProduct(product)
                if (response.isSuccessful && response.body()?.status == "success") {
                    dialog.dismiss()
                    Toast.makeText(this@ProductListActivity, "Produk Berhasil Ditambahkan", Toast.LENGTH_SHORT).show()
                    loadProducts()
                } else {
                    Toast.makeText(
                        this@ProductListActivity,
                        "Gagal menyimpan produk (${response.code()})",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProductListActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getAllProducts()
                if (response.isSuccessful && response.body()?.status == "success") {
                    val products = response.body()?.data ?: emptyList()
                    adapter.setData(products)
                } else {
                    Toast.makeText(this@ProductListActivity, "Gagal memuat produk", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProductListActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class ProductAdapter : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {
        private var products: List<Product> = emptyList()

        fun setData(newProducts: List<Product>) {
            products = newProducts
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val product = products[position]
            holder.bind(product)
        }

        override fun getItemCount() = products.size

        inner class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
            private val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
            private val tvName: TextView = view.findViewById(R.id.tvName)
            private val tvBarcode: TextView = view.findViewById(R.id.tvBarcode)
            private val tvPrice: TextView = view.findViewById(R.id.tvPrice)
            private val tvStock: TextView = view.findViewById(R.id.tvStock)

            fun bind(product: Product) {
                tvName.text = product.name ?: "Tanpa Nama"
                tvBarcode.text = product.barcode ?: "-"

                val format = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
                tvPrice.text = runCatching { format.format(product.price ?: 0.0) }.getOrDefault("-")
                tvStock.text = "Stok: ${product.stock ?: 0.0}"

                val iconRes = when (product.icon) {
                    "egg" -> R.drawable.ic_lucide_egg
                    "milk" -> R.drawable.ic_lucide_milk
                    "wheat" -> R.drawable.ic_lucide_wheat
                    else -> R.drawable.ic_lucide_package
                }
                ivIcon.setImageResource(iconRes)
            }
        }
    }
}
