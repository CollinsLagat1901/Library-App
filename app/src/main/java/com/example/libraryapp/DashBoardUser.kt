package com.example.libraryapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout.AttachedBehavior
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.libraryapp.databinding.ActivityDashBoardUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashBoardUser : AppCompatActivity() {
    private lateinit var binding: ActivityDashBoardUserBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var categoryArrayList: java.util.ArrayList<ModelCategory>

    private lateinit var viewPagerAdapter: ViewPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board_user)

        binding = ActivityDashBoardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        setupWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)


        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainScreen::class.java))
            finish()
        }
    }

    private fun setupWithViewPagerAdapter(viewPager: ViewPager) {
        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            this
        )

        categoryArrayList = ArrayList()

        //load categories from db

        val ref = FirebaseDatabase.getInstance().getReference("categories")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list
                categoryArrayList.clear()

                    //add data to models
                val modelAll = ModelCategory("01", "All", 1, "")
                val modeMostViewed = ModelCategory("02", "Most Viewed", 1, "")
                val modeMostDownloaded = ModelCategory("04", "Most Downloaded", 1, "")
                val guide = ModelCategory("03", "Guides", 1, "")

                //add to lost list
                categoryArrayList.add(modelAll)
                categoryArrayList.add(modeMostViewed)
                categoryArrayList.add(modeMostDownloaded)
                //add to viewPagerAdapter
                viewPagerAdapter.addFragment(
                    BooksUserFragments.newInsatance(
                        categoryId = "${modelAll.id}",
                        category = "${modelAll.category}",
                        uid = "${modelAll.uid}"
                    ), modelAll.category
                )
                viewPagerAdapter.addFragment(
                    BooksUserFragments.newInsatance(
                        categoryId = "${modeMostViewed.id}",
                        category = "${modeMostViewed.category}",
                        uid = "${modeMostViewed.uid}"
                    ), modeMostViewed.category
                )
                viewPagerAdapter.addFragment(
                    BooksUserFragments.newInsatance(
                        categoryId = "${modeMostDownloaded.id}",
                        category = "${modeMostDownloaded.category}",
                        uid = "${modeMostDownloaded.uid}"
                    ), modeMostDownloaded.category
                )


                //refresh list
                viewPagerAdapter.notifyDataSetChanged()

                //now lead from firebase db
                for (ds in snapshot.children){
                    //get load from in model
                    val model= ds.getValue(ModelCategory::class.java)
                    //add list
                    categoryArrayList.add(model!!)
                    // add to viewpageradapter
                    viewPagerAdapter.addFragment(
                        BooksUserFragments.newInsatance(
                            categoryId = "${model.id}",
                            category = "${model.category}",
                            uid = "${model.uid}"
                        ), model.category
                    )

                    //refresh list
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        //setup adapter to viewpage
        viewPager.adapter =viewPagerAdapter
    }


    class ViewPagerAdapter(fm: FragmentManager, behavior: Int, context: Context) :
        FragmentPagerAdapter(fm, behavior) {
        private val fragmenList: ArrayList<Fragment> = ArrayList()

        private val fragmenTitleList: ArrayList<String> = ArrayList();
        private val context: Context

        init {
            this.context = context
        }

        override fun getCount(): Int {
            return fragmenList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmenList[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmenTitleList[position]
        }

        public fun addFragment(fragment: BooksUserFragments, title: String) {
            fragmenList.add(fragment)

            fragmenTitleList.add(title)
        }


    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            // Not logged in, go to the main screen
            binding.subTitleTv.text = "Not Logged In"
        } else {
            // Logged in, get and show user info
            val email = firebaseUser.email
            // Set to the TextView in the toolbar
            binding.subTitleTv.text = email
        }
    }
}