package edu.stanford.qiwen.starmap

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.stanford.qiwen.starmap.models.MapDao
import edu.stanford.qiwen.starmap.models.Place
import edu.stanford.qiwen.starmap.models.UserMap
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"
const val EXTRA_USER_MAP = "EXTRA_USER_MAP"
const val EXTRA_MAP_TITLE = "EXTRA_MAP_TITLE"
private const val REQUEST_CODE = 1234

class MainActivity : AppCompatActivity() {

    private lateinit var userMaps: MutableList<UserMap>
    private lateinit var mapAdapter: MapsAdapter
    private lateinit var searchView: SearchView
    private lateinit var colorDrawableBackground: ColorDrawable
    private lateinit var deleteIcon: Drawable

    private lateinit var mapDao: MapDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get UserMaps from Databse
        mapDao = StarMapApp.getDatabase()!!.mapDao()
        GlobalScope.launch {
            userMaps = mapDao.getAll().toMutableList()
            if (userMaps.isEmpty()) {
                userMaps = generateSampleData().toMutableList()
                mapDao.insertMapAll(generateSampleData())
            }

            Log.i(TAG, "Database num items: ${userMaps.size}")
            // Render the RecycleList after loading the user maps
            // Set adapter on the recycler view
            mapAdapter = MapsAdapter(this@MainActivity, userMaps, object : MapsAdapter.OnClickListener {
                override fun onItemClick(userMap: UserMap) {
                    // When user taps on view in RV, navigate to new activity
                    val intent = Intent(this@MainActivity, DisplayMapActivity::class.java)
                    intent.putExtra(EXTRA_USER_MAP, userMap)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            })

            this@MainActivity.runOnUiThread {
                // Set layout manager on the recycler view
                rvMaps.layoutManager = LinearLayoutManager(this@MainActivity)
                rvMaps.adapter = mapAdapter
            }
        }
        // Create new map dialog
        fabCreateMap.setOnClickListener {
            showAlertDialog()
        }

        // Delete Icon
        colorDrawableBackground = ColorDrawable(Color.parseColor("#ff0000"))
        deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete)!!

    }

    private fun showAlertDialog() {
        val mapFormView = LayoutInflater.from(this).inflate(R.layout.dialog_create_map, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Enter the Map Name")
            .setView(mapFormView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK", null)
            .show()

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val title = mapFormView.findViewById<EditText>(R.id.etTitle).text.toString()
            if (title.trim().isEmpty()) {
                Toast.makeText(
                    this, "Place must have non-empty title",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            // Navigate to create map activity

            val intent = Intent(this@MainActivity, CreateMapActivity::class.java)
            intent.putExtra(EXTRA_MAP_TITLE, title)
            startActivityForResult(intent, REQUEST_CODE)
            dialog.dismiss()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Get new data from map
            val userMap = data?.getSerializableExtra(EXTRA_USER_MAP) as UserMap
            Log.i(TAG, "onActivityResult with new map title ${userMap.title}")
            userMaps.add(userMap)
            // Insert the new userMap into the UserMapDatabase
            GlobalScope.launch {
                mapDao.insertMap(userMap)
                Log.i(TAG, "Database item insert. Num item now: " + mapDao.getAll().size)
            }
            // Update View
            mapAdapter.filter.filter(searchView.query)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        // Search Item
        val searchMenuItem: MenuItem = menu?.getItem(0) as MenuItem
        searchView = searchMenuItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.i(TAG,"onQueryTextSubmit: $query")
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Log.i(TAG,"onQueryTextChange: $newText")
                mapAdapter.filter.filter(newText)
                return true
            }
        })
        searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                // Clear search text
                searchView.setQuery("", false)
//                mapAdapter.filter.filter("")
                return true
            }

        })

        searchView.imeOptions = EditorInfo.IME_ACTION_DONE

        /* Source: https://syntaxbytetutorials.com/android-recyclerview-swipe-to-delete-and-undo-with-itemtouchhelper/ */
        // Delete item animation
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, viewHolder2: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDirection: Int) {
                val userMapToDelete = userMaps[viewHolder.adapterPosition]
                // Remove the item from the list
                GlobalScope.launch {
                    // Remove the item in the Database
                    mapDao.deleteMap(userMapToDelete.title)
                    Log.i(TAG, "Database item deleted. Num item now: " + mapDao.getAll().size)
                }
                userMaps.removeAt(viewHolder.adapterPosition)
                mapAdapter.filter.filter(searchView.query)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val iconMarginVertical = (viewHolder.itemView.height - deleteIcon.intrinsicHeight) / 2

                if (dX > 0) {
                    colorDrawableBackground.setBounds(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                    deleteIcon.setBounds(itemView.left + iconMarginVertical, itemView.top + iconMarginVertical,
                        itemView.left + iconMarginVertical + deleteIcon.intrinsicWidth, itemView.bottom - iconMarginVertical)
                } else {
                    colorDrawableBackground.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    deleteIcon.setBounds(itemView.right - iconMarginVertical - deleteIcon.intrinsicWidth, itemView.top + iconMarginVertical,
                        itemView.right - iconMarginVertical, itemView.bottom - iconMarginVertical)
                    deleteIcon.level = 0
                }
                colorDrawableBackground.draw(c)
                c.save()

                if (dX > 0)
                    c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                else
                    c.clipRect(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                deleteIcon.draw(c)
                c.restore()
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(rvMaps)

        return super.onCreateOptionsMenu(menu)
    }

    private fun generateSampleData(): List<UserMap> {
        return listOf(
            UserMap(
                "Memories from University",
                listOf(
                    Place("Branner Hall", "Best dorm at Stanford", 37.426, -122.163),
                    Place(
                        "Gates CS building",
                        "Many long nights in this basement",
                        37.430,
                        -122.173
                    ),
                    Place("Pinkberry", "First date with my wife", 37.444, -122.170)
                )
            ),
            UserMap(
                "January vacation planning!",
                listOf(
                    Place("Tokyo", "Overnight layover", 35.67, 139.65),
                    Place("Ranchi", "Family visit + wedding!", 23.34, 85.31),
                    Place("Singapore", "Inspired by \"Crazy Rich Asians\"", 1.35, 103.82)
                )
            ),
            UserMap(
                "Singapore travel itinerary",
                listOf(
                    Place("Gardens by the Bay", "Amazing urban nature park", 1.282, 103.864),
                    Place(
                        "Jurong Bird Park",
                        "Family-friendly park with many varieties of birds",
                        1.319,
                        103.706
                    ),
                    Place("Sentosa", "Island resort with panoramic views", 1.249, 103.830),
                    Place(
                        "Botanic Gardens",
                        "One of the world's greatest tropical gardens",
                        1.3138,
                        103.8159
                    )
                )
            ),
            UserMap(
                "My favorite places in the Midwest",
                listOf(
                    Place(
                        "Chicago",
                        "Urban center of the midwest, the \"Windy City\"",
                        41.878,
                        -87.630
                    ),
                    Place("Rochester, Michigan", "The best of Detroit suburbia", 42.681, -83.134),
                    Place(
                        "Mackinaw City",
                        "The entrance into the Upper Peninsula",
                        45.777,
                        -84.727
                    ),
                    Place("Michigan State University", "Home to the Spartans", 42.701, -84.482),
                    Place("University of Michigan", "Home to the Wolverines", 42.278, -83.738)
                )
            ),
            UserMap(
                "Restaurants to try",
                listOf(
                    Place("Champ's Diner", "Retro diner in Brooklyn", 40.709, -73.941),
                    Place("Althea", "Chicago upscale dining with an amazing view", 41.895, -87.625),
                    Place("Shizen", "Elegant sushi in San Francisco", 37.768, -122.422),
                    Place(
                        "Citizen Eatery",
                        "Bright cafe in Austin with a pink rabbit",
                        30.322,
                        -97.739
                    ),
                    Place(
                        "Kati Thai",
                        "Authentic Portland Thai food, served with love",
                        45.505,
                        -122.635
                    )
                )
            )
        )
    }

}
