# expandable-textview

TextView for Android with expand and collapse feature.

- Custom hint is applied if the text is greater than the max lines defined. If no hint text is defined, the default ellipsis is used.
- Animation time for expand and collapse can be defined. Zero for non animation (default).

## Usage

- xml: expand action hint with default style, bold and underlined, will be used.

```xml
    <br.com.amd.expandabletextview.ExpandableTextView
        android:id="@+id/theText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@string/long_text"
        app:expandActionHint ="show more"
        app:collapsedMaxLines="3"
        ... />
```
- code: set animation time in millis and listeners for expand and collapse actions (all optional)

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with (binding.theText) {
            setAnimationDurationTime(
                lifecycleOwner = this@MainActivity,
                timeInMillis = 300
            )
            
            setOnCollapsedListener {
                Toast.makeText(this@MainActivity, "Collapsed!", Toast.LENGTH_SHORT).show()
            }

            setOnExpandedListener {
                Toast.makeText(this@MainActivity, "Expanded!", Toast.LENGTH_SHORT).show()
            }
        }
    }
(...)
```
### Result

https://user-images.githubusercontent.com/47090245/139585547-1907ed60-6069-4126-a697-d13d3beda19b.mp4


- Expand action hint can be set with your custom style

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with (binding.theText) {
            val actionHintStr = "click for more"
            val actionHint = buildSpannedString {
                append(actionHintStr)
                setSpan(StyleSpan(Typeface.BOLD), 0, actionHintStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(UnderlineSpan(), 0, actionHintStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(ForegroundColorSpan(Color.RED), 0, actionHintStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            setExpandActionHint(actionHint)
            
            setAnimationDurationTime(
                lifecycleOwner = this@MainActivity,
                timeInMillis = 300
            )
            
            setOnCollapsedListener {
                Toast.makeText(this@MainActivity, "Collapsed!", Toast.LENGTH_SHORT).show()
            }

            setOnExpandedListener {
                Toast.makeText(this@MainActivity, "Expanded!", Toast.LENGTH_SHORT).show()
            }
        }
    }
(...)
```

### Result

https://user-images.githubusercontent.com/47090245/139588697-df1a1f32-2d11-4876-b6bb-2231a44b86bc.mp4


