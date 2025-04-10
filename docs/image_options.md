# ImageOptions

Translations: [简体中文](image_options.zh.md)

[ImageOptions] is used to define image request configurations in batches and supports all
image-related attributes of [ImageRequest].

[ImageOptions] can be used in the following locations:

* [Target].getImageOptions()
    * [SketchImageView].imageOptions
    * [rememberAsyncImageState(ImageOptions)][AsyncImageState]
* [ImageRequest].Builder.merge(ImageOptions)/default(ImageOptions)
* [Sketch].Builder.globalImageOptions(ImageOptions)

The final priority of the same properties when constructing the [ImageRequest] is:

1. [ImageRequest].Builder
2. [Target].getImageOptions()
3. [ImageRequest].Builder.default(ImageOptions)
4. [Sketch].globalImageOptions

### Example

Global：

```kotlin
Sketch.Builder(context).apply {
    globalImageOptions(ImageOptions {
        placeholer(R.drawable.placeholder)
        error(R.drawable.error)
        // more ...
    })
}.build()
```

View：

```kotlin
sketchImageView.imageOptions = ImageOptions {
    placeholer(R.drawable.placeholder)
    // more ...
}
```

ImageRequest：

```kotlin
ImageRequest(context, "https://example.com/image.jpg") {
    merge(ImageOptions {
        placeholer(R.drawable.placeholder)
        error(R.drawable.error)
        // more ...
    })
    default(ImageOptions {
        placeholer(R.drawable.placeholder)
        error(R.drawable.error)
        // more ...
    })
}
```

AsyncImageState：

```kotlin
val state = rememberAsyncImageState(ComposableImageOptions {
  placeholer(Res.drawable.placeholder)
  error(Res.drawable.error)
  // more ...
})
AsyncImage(
  uri = "https://example.com/image.jpg",
  contentDescription = "",
  state = state,
)
```

[Sketch]: ../sketch-core/src/commonMain/kotlin/com/github/panpf/sketch/Sketch.common.kt

[Target]: ../sketch-core/src/commonMain/kotlin/com/github/panpf/sketch/target/Target.kt

[ImageRequest]: ../sketch-core/src/commonMain/kotlin/com/github/panpf/sketch/request/ImageRequest.common.kt

[ImageOptions]: ../sketch-core/src/commonMain/kotlin/com/github/panpf/sketch/request/ImageOptions.common.kt

[SketchImageView]: ../sketch-extensions-view/src/main/kotlin/com/github/panpf/sketch/SketchImageView.kt

[AsyncImageState]: ../sketch-compose-core/src/commonMain/kotlin/com/github/panpf/sketch/AsyncImageState.kt