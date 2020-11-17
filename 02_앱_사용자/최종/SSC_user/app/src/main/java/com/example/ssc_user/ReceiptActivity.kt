package com.example.ssc_user

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_receipt.*
import kotlinx.android.synthetic.main.detail_receipt.view.*

class ReceiptActivity : AppCompatActivity() {
    var firestore: FirebaseFirestore? = null
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    var user = mAuth.currentUser?.email.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)
        var total = intent.getStringExtra("total")
        var datetime = intent.getStringExtra("datetime")

        product11.setText("상품명")
        price11.setText("가격")
        quantity11.setText("수량")
        totalprice11.setText("금액")
        rec_date.setText(datetime)

        firestore = FirebaseFirestore.getInstance()
        detail_recyclerview.adapter = RecyclerViewAdapter()
        detail_recyclerview.layoutManager = LinearLayoutManager(this)

        p.setText("합계금액")

        total_total.setText("$total"+"원")


    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        // Person 클래스 ArrayList 생성성
        var Purchase : ArrayList<ProductData> = arrayListOf()
        var datetime = intent.getStringExtra("datetime")

        init {
            firestore?.collection("Purchase")
                ?.whereEqualTo("user_id", "$user")
                ?.whereEqualTo("datetime", "$datetime")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    Purchase.clear()

                    for (snapshot in querySnapshot!!.documents) {
                        val items = snapshot.data?.get("each_product") as List<Map<String, Any>>
                        for(item in items) {
                            var data = ProductData(
                                    item.get("product") as String,
                                    item.get("quantity") as Long,
                                    item.get("price") as Long,
                                    )
                            Purchase.add(data)
                        }
                    }
                    notifyDataSetChanged()
                }
        }

        // xml파일을 inflate하여 ViewHolder를 생성
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.detail_receipt, parent, false)
            return ViewHolder(view)
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        }

        // onCreateViewHolder에서 만든 view와 실제 데이터를 연결
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewHolder = (holder as ViewHolder).itemView

            viewHolder.detail_product?.text = Purchase[position].product
            viewHolder.detail_price?.text = Purchase[position].price.toString()
            viewHolder.detail_quantity?.text = Purchase[position].quantity.toString()

            var price = Purchase[position].price
            var quantity = Purchase[position].quantity
            if (price != null) {
                viewHolder.detail_total_price.text = (price* quantity!!).toString()
            }
        }
        // 리사이클러뷰의 아이템 총 개수 반환
        override fun getItemCount(): Int {
            return Purchase.size
        }
    }
}