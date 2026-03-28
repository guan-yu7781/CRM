<script setup>
import { computed } from 'vue';

const props = defineProps({
  activeMarkets: { type: Array, default: () => [] }
});

// Country centroids [lat, lon]
const COORDS = {
  'Algeria': [28, 3], 'Angola': [-12, 18], 'Benin': [9, 2], 'Botswana': [-22, 24],
  'Burkina Faso': [12, -1], 'Burundi': [-3.5, 30], 'Cabo Verde': [16, -24],
  'Cameroon': [5.7, 12], 'Central African Republic': [6.6, 21], 'Chad': [15, 19],
  'Comoros': [-12, 44], 'Congo': [-0.2, 16], "Cote d'Ivoire": [6.5, -5.5],
  'Democratic Republic of the Congo': [-2.9, 24], 'Djibouti': [12, 43],
  'Egypt': [27, 30], 'Equatorial Guinea': [2, 10], 'Eritrea': [15, 40],
  'Eswatini': [-26.5, 31.5], 'Ethiopia': [9, 40], 'Gabon': [-0.8, 12],
  'Gambia': [13.5, -15], 'Ghana': [7.9, -1], 'Guinea': [11, -11],
  'Guinea-Bissau': [12, -15], 'Kenya': [0.5, 38], 'Lesotho': [-29.6, 28],
  'Liberia': [6.4, -9], 'Libya': [27, 17], 'Madagascar': [-20, 47],
  'Malawi': [-13, 34], 'Mali': [17, -2], 'Mauritania': [20, -11],
  'Mauritius': [-20, 57.5], 'Morocco': [32, -7], 'Mozambique': [-19, 35],
  'Namibia': [-22, 18.5], 'Niger': [16, 8], 'Nigeria': [9, 8],
  'Rwanda': [-2, 30], 'Sao Tome and Principe': [0.2, 6.6], 'Senegal': [14.5, -14.5],
  'Seychelles': [-5, 55.5], 'Sierra Leone': [8.4, -12], 'Somalia': [5, 46],
  'South Africa': [-29, 25], 'South Sudan': [7.9, 31], 'Sudan': [15, 30],
  'Tanzania': [-6, 35], 'Togo': [8, 1], 'Tunisia': [34, 9],
  'Uganda': [1.4, 32], 'Zambia': [-13, 28], 'Zimbabwe': [-20, 30],
  'Afghanistan': [34, 68], 'Armenia': [40, 45], 'Azerbaijan': [40.5, 47.5],
  'Bahrain': [26, 50.5], 'Bangladesh': [24, 90], 'Bhutan': [27.5, 90],
  'Brunei Darussalam': [4.5, 115], 'Cambodia': [12.5, 105], 'China': [36, 104],
  'Cyprus': [35, 33], 'Georgia': [42, 43.5], 'India': [22, 79],
  'Indonesia': [-1, 114], 'Iran': [32, 54], 'Iraq': [33, 44],
  'Israel': [31.5, 35], 'Japan': [37, 138], 'Jordan': [31, 37],
  'Kazakhstan': [48, 68], 'Kuwait': [29.5, 47.5], 'Kyrgyzstan': [41, 75],
  "Lao People's Democratic Republic": [18, 104], 'Lebanon': [34, 36],
  'Malaysia': [4, 109.5], 'Maldives': [3, 73], 'Mongolia': [46.5, 103],
  'Myanmar': [19, 96], 'Nepal': [28, 84], "Democratic People's Republic of Korea": [40, 127],
  'Oman': [21.5, 57], 'Pakistan': [30, 69], 'Philippines': [13, 122],
  'Qatar': [25, 51], 'Republic of Korea': [37, 128], 'Saudi Arabia': [24, 45],
  'Singapore': [1.3, 104], 'Sri Lanka': [8, 81], 'State of Palestine': [32, 35],
  'Syrian Arab Republic': [35, 38], 'Tajikistan': [38.5, 71], 'Thailand': [16, 101],
  'Timor-Leste': [-8.9, 126], 'Turkiye': [39, 35], 'Turkmenistan': [39, 59],
  'United Arab Emirates': [24, 54], 'Uzbekistan': [41.5, 64.5],
  'Viet Nam': [14, 108], 'Yemen': [16, 48]
};

const W = 900, H = 450;

function xy(lat, lon) {
  return [(lon + 180) * W / 360, (90 - lat) * H / 180];
}

const activeSet = computed(() => new Set(props.activeMarkets));

const markers = computed(() =>
  Object.entries(COORDS).map(([name, [lat, lon]]) => {
    const [x, y] = xy(lat, lon);
    return { name, x, y, active: activeSet.value.has(name) };
  })
);

const activeMarkerList = computed(() =>
  markers.value.filter(m => m.active).map(m => m.name).sort()
);
</script>

<template>
  <div class="world-map-panel">
    <svg class="world-map-svg" viewBox="0 0 900 450" xmlns="http://www.w3.org/2000/svg" aria-label="Business presence world map">
      <!-- Ocean -->
      <rect width="900" height="450" fill="#0d1f1a"/>

      <!-- North America -->
      <polygon points="30,75 85,55 140,45 175,50 210,60 245,80 262,105 270,132 265,158 252,178 242,200 228,218 215,222 200,215 182,200 162,180 145,162 125,148 100,135 75,122 50,108" fill="#162b24" stroke="#1e3d30" stroke-width="0.8"/>
      <!-- Greenland -->
      <ellipse cx="378" cy="50" rx="22" ry="28" fill="#162b24" stroke="#1e3d30" stroke-width="0.8"/>
      <!-- Central America -->
      <polygon points="242,200 232,215 222,230 230,238 240,230 248,218" fill="#162b24" stroke="#1e3d30" stroke-width="0.8"/>
      <!-- South America -->
      <polygon points="248,240 268,228 295,228 318,238 335,255 345,278 342,310 330,338 315,358 295,368 272,362 255,345 244,320 238,295 242,268" fill="#162b24" stroke="#1e3d30" stroke-width="0.8"/>

      <!-- Africa -->
      <polygon points="435,135 478,128 510,128 533,138 543,153 552,180 558,195 578,198 558,225 552,235 550,243 538,253 538,292 532,296 495,313 490,305 480,295 480,270 480,240 472,235 472,218 465,215 452,213 438,215 422,213 413,208 410,195 408,178 418,155 430,148" fill="#162b24" stroke="#1e3d30" stroke-width="0.8"/>
      <!-- Madagascar -->
      <ellipse cx="568" cy="280" rx="7" ry="17" fill="#162b24" stroke="#1e3d30" stroke-width="0.8" transform="rotate(-10 568 280)"/>

      <!-- Europe (simplified as part of Eurasia) -->
      <polygon points="428,133 445,118 462,108 478,100 492,95 505,100 512,110 505,120 488,125 472,120 455,120 440,122" fill="#162b24" stroke="#1e3d30" stroke-width="0.8"/>

      <!-- Main Asia body -->
      <polygon points="440,122 462,108 492,95 530,82 580,72 635,65 685,62 730,65 778,72 815,82 840,100 838,118 820,132 802,145 780,162 760,178 745,198 730,215 715,235 700,255 685,258 662,248 642,225 622,212 600,205 582,200 568,188 555,170 545,155 535,140 520,130 505,120 488,125 472,120" fill="#162b24" stroke="#1e3d30" stroke-width="0.8"/>

      <!-- Arabian Peninsula -->
      <polygon points="540,152 558,150 572,162 580,180 578,200 562,218 548,213 538,200 535,178 538,160" fill="#162b24" stroke="#1e3d30" stroke-width="0.8"/>

      <!-- Indian subcontinent -->
      <polygon points="600,158 628,148 660,155 672,170 655,198 648,208 633,212 615,208 600,200 595,185 598,168" fill="#162b24" stroke="#1e3d30" stroke-width="0.8"/>

      <!-- SE Asia / Malay Peninsula -->
      <polygon points="712,175 730,170 748,178 752,195 742,215 728,225 712,220 700,215 698,198 705,185" fill="#162b24" stroke="#1e3d30" stroke-width="0.8"/>

      <!-- Indonesia (simplified) -->
      <ellipse cx="722" cy="248" rx="35" ry="8" fill="#162b24" stroke="#1e3d30" stroke-width="0.8"/>
      <ellipse cx="775" cy="252" rx="15" ry="6" fill="#162b24" stroke="#1e3d30" stroke-width="0.8"/>

      <!-- Japan -->
      <ellipse cx="803" cy="130" rx="7" ry="20" fill="#162b24" stroke="#1e3d30" stroke-width="0.8" transform="rotate(-25 803 130)"/>

      <!-- Australia -->
      <polygon points="730,272 762,258 800,262 825,278 830,308 812,322 790,325 762,318 738,310 728,295" fill="#162b24" stroke="#1e3d30" stroke-width="0.8"/>

      <!-- Inactive country dots -->
      <circle
        v-for="m in markers.filter(m => !m.active)"
        :key="m.name"
        :cx="m.x" :cy="m.y" r="2.5"
        fill="#2a4a3c" opacity="0.7"
      />

      <!-- Active country dots (glowing teal) -->
      <g v-for="m in markers.filter(m => m.active)" :key="`active-${m.name}`">
        <circle :cx="m.x" :cy="m.y" r="8" fill="#2f9e87" opacity="0.18"/>
        <circle :cx="m.x" :cy="m.y" r="4.5" fill="#2f9e87" opacity="0.85"/>
        <title>{{ m.name }}</title>
      </g>
    </svg>

    <div class="world-map-legend">
      <div class="world-map-legend-items">
        <span class="world-map-legend-dot world-map-legend-dot--active"/>
        <span>Active markets ({{ activeMarkerList.length }})</span>
        <span class="world-map-legend-dot world-map-legend-dot--inactive" style="margin-left:16px"/>
        <span>Known regions</span>
      </div>
      <div v-if="activeMarkerList.length" class="world-map-tags">
        <span v-for="name in activeMarkerList" :key="name" class="world-map-tag">{{ name }}</span>
      </div>
      <p v-else class="world-map-empty">No market presence data available.</p>
    </div>
  </div>
</template>
