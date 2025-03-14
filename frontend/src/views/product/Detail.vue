<img :src="getImageUrl(product.mainImage)" :alt="product.name" />

<div v-for="(img, index) in getImageUrls(product.images)" :key="index">
  <img :src="img" :alt="product.name + '-' + index" />
</div> 