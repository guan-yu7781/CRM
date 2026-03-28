<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import * as echarts from 'echarts/core';

const ZOOM_STEP = 1.5; // each button press zooms by ×1.5 or ÷1.5
import { EffectScatterChart } from 'echarts/charts';
import { GeoComponent, TooltipComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

echarts.use([EffectScatterChart, GeoComponent, TooltipComponent, CanvasRenderer]);

const props = defineProps({
  marketCounts: { type: Array, default: () => [] } // [{ name, count }]
});

const chartEl = ref(null);
let chart = null;

// Country name → [longitude, latitude]
const COORDS = {
  'Algeria': [3, 28], 'Angola': [18, -12], 'Benin': [2, 9], 'Botswana': [24, -22],
  'Burkina Faso': [-1, 12], 'Burundi': [30, -3.5], 'Cabo Verde': [-24, 16],
  'Cameroon': [12, 5.7], 'Central African Republic': [21, 6.6], 'Chad': [19, 15],
  'Comoros': [44, -12], 'Congo': [16, -0.2], "Cote d'Ivoire": [-5.5, 6.5],
  'Democratic Republic of the Congo': [24, -2.9], 'Djibouti': [43, 12],
  'Egypt': [30, 27], 'Equatorial Guinea': [10, 2], 'Eritrea': [40, 15],
  'Eswatini': [31.5, -26.5], 'Ethiopia': [40, 9], 'Gabon': [12, -0.8],
  'Gambia': [-15, 13.5], 'Ghana': [-1, 7.9], 'Guinea': [-11, 11],
  'Guinea-Bissau': [-15, 12], 'Kenya': [38, 0.5], 'Lesotho': [28, -29.6],
  'Liberia': [-9, 6.4], 'Libya': [17, 27], 'Madagascar': [47, -20],
  'Malawi': [34, -13], 'Mali': [-2, 17], 'Mauritania': [-11, 20],
  'Mauritius': [57.5, -20], 'Morocco': [-7, 32], 'Mozambique': [35, -19],
  'Namibia': [18.5, -22], 'Niger': [8, 16], 'Nigeria': [8, 9],
  'Rwanda': [30, -2], 'Sao Tome and Principe': [6.6, 0.2], 'Senegal': [-14.5, 14.5],
  'Seychelles': [55.5, -5], 'Sierra Leone': [-12, 8.4], 'Somalia': [46, 5],
  'South Africa': [25, -29], 'South Sudan': [31, 7.9], 'Sudan': [30, 15],
  'Tanzania': [35, -6], 'Togo': [1, 8], 'Tunisia': [9, 34],
  'Uganda': [32, 1.4], 'Zambia': [28, -13], 'Zimbabwe': [30, -20],
  'Afghanistan': [68, 34], 'Armenia': [45, 40], 'Azerbaijan': [47.5, 40.5],
  'Bahrain': [50.5, 26], 'Bangladesh': [90, 24], 'Bhutan': [90, 27.5],
  'Brunei Darussalam': [115, 4.5], 'Cambodia': [105, 12.5], 'China': [104, 36],
  'Cyprus': [33, 35], 'Georgia': [43.5, 42], 'India': [79, 22],
  'Indonesia': [114, -1], 'Iran': [54, 32], 'Iraq': [44, 33],
  'Israel': [35, 31.5], 'Japan': [138, 37], 'Jordan': [37, 31],
  'Kazakhstan': [68, 48], 'Kuwait': [47.5, 29.5], 'Kyrgyzstan': [75, 41],
  "Lao People's Democratic Republic": [104, 18], 'Lebanon': [36, 34],
  'Malaysia': [109.5, 4], 'Maldives': [73, 3], 'Mongolia': [103, 46.5],
  'Myanmar': [96, 19], 'Nepal': [84, 28], "Democratic People's Republic of Korea": [127, 40],
  'Oman': [57, 21.5], 'Pakistan': [69, 30], 'Philippines': [122, 13],
  'Qatar': [51, 25], 'Republic of Korea': [128, 37], 'Saudi Arabia': [45, 24],
  'Singapore': [104, 1.3], 'Sri Lanka': [81, 8], 'State of Palestine': [35, 32],
  'Syrian Arab Republic': [38, 35], 'Tajikistan': [71, 38.5], 'Thailand': [101, 16],
  'Timor-Leste': [126, -8.9], 'Turkiye': [35, 39], 'Turkmenistan': [59, 39],
  'United Arab Emirates': [54, 24], 'Uzbekistan': [64.5, 41.5],
  'Viet Nam': [108, 14], 'Yemen': [48, 16],
  'Albania': [20, 41], 'Austria': [14, 47.5], 'Belarus': [28, 53.5],
  'Belgium': [4.5, 50.8], 'Bosnia and Herzegovina': [17.5, 44], 'Bulgaria': [25, 43],
  'Croatia': [16, 45.5], 'Czech Republic': [15.5, 50], 'Denmark': [10, 56],
  'Estonia': [25, 59], 'Finland': [26, 64], 'France': [2, 46],
  'Germany': [10, 51], 'Greece': [22, 39], 'Hungary': [19, 47],
  'Ireland': [-8, 53], 'Italy': [12, 42.8], 'Latvia': [25, 57],
  'Lithuania': [24, 56], 'Luxembourg': [6.1, 49.8], 'Malta': [14.4, 35.9],
  'Moldova': [29, 47], 'Montenegro': [19, 42.5], 'Netherlands': [5.3, 52.3],
  'North Macedonia': [21.7, 41.6], 'Norway': [10, 62], 'Poland': [20, 52],
  'Portugal': [-8, 39.5], 'Romania': [25, 46], 'Russia': [100, 60],
  'Serbia': [21, 44], 'Slovakia': [19.5, 48.7], 'Slovenia': [15, 46.1],
  'Spain': [-4, 40], 'Sweden': [15, 62], 'Switzerland': [8, 47],
  'Ukraine': [32, 49], 'United Kingdom': [-3, 54],
  'Canada': [-96, 56], 'United States of America': [-98, 38], 'Mexico': [-102, 24],
  'Brazil': [-53, -10], 'Argentina': [-64, -34], 'Colombia': [-74, 4],
  'Chile': [-71, -30], 'Peru': [-76, -10], 'Venezuela': [-66, 8],
  'Ecuador': [-77.5, -1.8], 'Bolivia': [-65, -17], 'Paraguay': [-58, -23],
  'Uruguay': [-56, -33], 'Australia': [133, -27], 'New Zealand': [174, -41]
};

function buildOption() {
  const maxCount = Math.max(...props.marketCounts.map(m => m.count), 1);

  const data = props.marketCounts
    .filter(({ name }) => COORDS[name])
    .map(({ name, count }) => ({
      name,
      value: [...COORDS[name], count]
    }));

  return {
    backgroundColor: '#f5f8f7',
    tooltip: {
      trigger: 'item',
      backgroundColor: '#fff',
      borderColor: '#d0e8e0',
      textStyle: { color: '#1a3830', fontSize: 12 },
      formatter: p => p.data
        ? `<strong style="color:#2f9e87">${p.data.name}</strong><br/>${p.data.value[2]} record${p.data.value[2] !== 1 ? 's' : ''}`
        : ''
    },
    geo: {
      map: 'world',
      roam: true,     // pan + zoom enabled; scroll-wheel blocked via JS
      zoom: 2.0,
      center: [22, 5],
      itemStyle: {
        areaColor: '#ddeee8',
        borderColor: '#b8d8cc',
        borderWidth: 0.6
      },
      emphasis: {
        itemStyle: { areaColor: '#c2ddd4' },
        label: { show: false }
      },
      label: { show: false }
    },
    series: [
      {
        type: 'effectScatter',
        coordinateSystem: 'geo',
        data,
        symbolSize: d => {
          const base = Math.sqrt(d[2] / maxCount) * 28;
          return Math.max(8, base);
        },
        itemStyle: { color: '#2f9e87', opacity: 0.9 },
        rippleEffect: {
          brushType: 'stroke',
          color: '#2f9e87',
          scale: 3.5,
          period: 4
        },
        emphasis: {
          itemStyle: { color: '#1d7a65' },
          label: {
            show: true,
            formatter: p => p.data.name,
            color: '#1a3830',
            fontSize: 11,
            position: 'top'
          }
        }
      }
    ]
  };
}

let mapReady = false;

async function initChart() {
  if (!mapReady) {
    const res = await fetch('/world.json');
    const worldJson = await res.json();
    echarts.registerMap('world', worldJson);
    mapReady = true;
  }

  if (!chart && chartEl.value) {
    chart = echarts.init(chartEl.value, null, { renderer: 'canvas' });
  }
  chart?.setOption(buildOption());
}

function onResize() {
  chart?.resize();
}

onMounted(async () => {
  await initChart();
  window.addEventListener('resize', onResize);
  // Block scroll-wheel zoom on the chart canvas only
  chartEl.value?.addEventListener('wheel', blockWheelZoom, { passive: false });
});

onUnmounted(() => {
  chartEl.value?.removeEventListener('wheel', blockWheelZoom);
  window.removeEventListener('resize', onResize);
  chart?.dispose();
  chart = null;
});

function zoomIn() {
  chart?.dispatchAction({ type: 'geoRoam', geoIndex: 0, zoom: ZOOM_STEP });
}

function zoomOut() {
  chart?.dispatchAction({ type: 'geoRoam', geoIndex: 0, zoom: 1 / ZOOM_STEP });
}

// Block scroll-wheel zoom while keeping mouse-drag pan and touch gestures
function blockWheelZoom(e) {
  e.preventDefault();
  e.stopPropagation();
}

watch(() => props.marketCounts, () => {
  chart?.setOption(buildOption());
}, { deep: true });
</script>

<template>
  <div class="world-map-panel">
    <div class="world-map-chart-wrap">
      <div ref="chartEl" class="world-map-echarts" />
      <div class="map-zoom-controls">
        <button class="map-zoom-btn" type="button" @click="zoomIn" title="Zoom in">+</button>
        <button class="map-zoom-btn" type="button" @click="zoomOut" title="Zoom out">−</button>
      </div>
    </div>
    <div class="world-map-legend">
      <div class="world-map-legend-items">
        <span class="world-map-legend-dot world-map-legend-dot--active" />
        <span>Active markets ({{ marketCounts.length }})</span>
      </div>
      <div v-if="marketCounts.length" class="world-map-tags">
        <span
          v-for="m in [...marketCounts].sort((a,b) => b.count - a.count)"
          :key="m.name"
          class="world-map-tag"
        >{{ m.name }} <em>{{ m.count }}</em></span>
      </div>
      <p v-else class="world-map-empty">No market presence data available.</p>
    </div>
  </div>
</template>

<style scoped>
.world-map-chart-wrap {
  position: relative;
}

.world-map-echarts {
  width: 100%;
  height: 560px;
}

.map-zoom-controls {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 4px;
  z-index: 10;
}

.map-zoom-btn {
  width: 30px;
  height: 30px;
  background: #fff;
  border: 1px solid #c8ddd6;
  border-radius: 6px;
  font-size: 1.1rem;
  line-height: 1;
  color: #2f9e87;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s, border-color 0.15s;
  user-select: none;
}

.map-zoom-btn:hover {
  background: #edf7f4;
  border-color: #2f9e87;
}

.map-zoom-btn:active {
  background: #d4ede7;
}
</style>
