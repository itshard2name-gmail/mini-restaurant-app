<template>
  <div class="space-y-6">
    <!-- Stats Grid -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div v-for="(stat, index) in stats" :key="index" 
           :class="['rounded-2xl shadow-lg p-6 flex flex-col justify-between transition-all hover:scale-105 hover:shadow-xl relative overflow-hidden', stat.gradient]">
         
         <!-- Decorative Background Circle -->
         <div class="absolute -right-6 -top-6 w-32 h-32 rounded-full bg-white opacity-10 blur-2xl"></div>

         <div class="relative z-10 flex justify-between items-start">
             <div>
                <p class="text-sm font-medium text-white opacity-90">{{ stat.label }}</p>
                <h3 class="text-3xl font-extrabold text-white mt-1">{{ stat.value }}</h3>
             </div>
             <div class="p-2 rounded-lg bg-white bg-opacity-20 text-white backdrop-blur-sm">
                <component :is="stat.icon" class="w-6 h-6" />
             </div>
         </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
       <!-- Revenue Trend Chart -->
       <div class="lg:col-span-2 bg-card rounded-2xl shadow-sm border border-border p-6">
          <div class="flex items-center justify-between mb-6">
             <div>
                <h3 class="text-lg font-bold text-card-foreground">Revenue Trends</h3>
                <p class="text-xs text-muted-foreground">Last 7 days performance</p>
             </div>
          </div>
          <div class="h-80" v-if="!loading">
             <apexchart 
               width="100%" 
               height="100%" 
               type="area" 
               :options="chartOptions" 
               :series="series"
             ></apexchart>
          </div>
          <div v-else class="h-80 flex items-center justify-center text-gray-400">Loading Chart...</div>
       </div>

       <!-- Revenue Distribution Pie -->
       <div class="bg-card rounded-2xl shadow-sm border border-border p-6 flex flex-col">
           <h3 class="text-lg font-bold text-card-foreground mb-6">Revenue Mix</h3>
           <div class="h-64 flex-1" v-if="!loading">
              <apexchart 
                width="100%" 
                height="100%" 
                type="donut" 
                :options="pieOptions" 
                :series="pieSeries"
              ></apexchart>
           </div>
           
           <!-- AOV Stats -->
           <div class="mt-4 grid grid-cols-2 gap-4 border-t border-border pt-4">
               <div class="text-center">
                   <p class="text-xs text-muted-foreground uppercase">Dine-In AOV</p>
                   <p class="text-lg font-bold text-indigo-500">${{ aovStats.dineIn.toFixed(2) }}</p>
               </div>
               <div class="text-center">
                   <p class="text-xs text-muted-foreground uppercase">Takeout AOV</p>
                   <p class="text-lg font-bold text-emerald-500">${{ aovStats.takeout.toFixed(2) }}</p>
               </div>
           </div>
       </div>

       <!-- Top Items -->
       <div class="lg:col-span-3 bg-card rounded-2xl shadow-sm border border-border p-6 flex flex-col">
          <h3 class="text-lg font-bold text-card-foreground mb-6">Top Selling Items</h3>
          
          <div class="space-y-5 flex-1 overflow-y-auto pr-2 custom-scrollbar">
             <div v-for="(item, idx) in topItems" :key="idx" class="relative group">
                <div class="flex items-center justify-between z-10 relative mb-1">
                    <div class="flex items-center space-x-3 w-full max-w-[70%]">
                       <!-- Rank Badge -->
                       <div v-if="idx === 0" class="flex-shrink-0 w-8 h-8 rounded-full bg-yellow-900/40 text-yellow-400 flex items-center justify-center font-bold ring-2 ring-yellow-700/50 text-base shadow-sm">🥇</div>
                       <div v-else-if="idx === 1" class="flex-shrink-0 w-8 h-8 rounded-full bg-gray-700 text-gray-300 flex items-center justify-center font-bold ring-2 ring-gray-600 text-base shadow-sm">🥈</div>
                       <div v-else-if="idx === 2" class="flex-shrink-0 w-8 h-8 rounded-full bg-orange-900/40 text-orange-400 flex items-center justify-center font-bold ring-2 ring-orange-700/50 text-base shadow-sm">🥉</div>
                       <div v-else class="flex-shrink-0 w-8 h-8 rounded-full bg-muted text-muted-foreground flex items-center justify-center font-bold text-sm border border-border">{{ idx + 1 }}</div>

                       <span class="font-semibold text-card-foreground truncate text-sm">{{ item.name }}</span>
                    </div>
                    <span class="text-sm font-bold text-card-foreground">{{ item.quantity }} <span class="text-xs font-normal text-muted-foreground ml-0.5">sold</span></span>
                </div>
                <!-- Progress Bar -->
                <div class="h-2 w-full bg-muted rounded-full overflow-hidden">
                    <div class="h-full rounded-full transition-all duration-1000 ease-out"
                         :class="{
                            'bg-yellow-400': idx === 0,
                            'bg-gray-400': idx === 1,
                            'bg-orange-400': idx === 2,
                            'bg-indigo-400': idx > 2
                         }"
                         :style="{ width: `${item.percentage}%` }">
                    </div>
                </div>
             </div>
             
             <div v-if="topItems.length === 0 && !loading" class="text-center text-gray-400 py-10 flex flex-col items-center">
                <p>No sales data yet</p>
             </div>
          </div>
       </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, markRaw, h } from 'vue';
import VueApexCharts from 'vue3-apexcharts';
import axios from 'axios';

// Register ApexCharts locally
const apexchart = VueApexCharts;

// Mock Icons using render functions (avoid runtime template compilation)
const CurrencyDollarIconComp = { 
  render: () => h('svg', { xmlns: 'http://www.w3.org/2000/svg', fill: 'none', viewBox: '0 0 24 24', 'stroke-width': '1.5', stroke: 'currentColor' }, [
    h('path', { 'stroke-linecap': 'round', 'stroke-linejoin': 'round', d: 'M12 6v12m-3-2.818l.879.659c1.171.879 3.07.879 4.242 0 1.172-.879 1.172-2.303 0-3.182C13.536 12.219 12.768 12 12 12c-.725 0-1.45-.22-2.003-.659-1.106-.879-1.106-2.303 0-3.182s2.9-.879 4.006 0l.415.33M21 12a9 9 0 11-18 0 9 9 0 0118 0z' })
  ])
};
const ShoppingBagIconComp = { 
  render: () => h('svg', { xmlns: 'http://www.w3.org/2000/svg', fill: 'none', viewBox: '0 0 24 24', 'stroke-width': '1.5', stroke: 'currentColor' }, [
    h('path', { 'stroke-linecap': 'round', 'stroke-linejoin': 'round', d: 'M15.75 10.5V6a3.75 3.75 0 10-7.5 0v4.5m11.356-1.993l1.263 12c.07.665-.45 1.243-1.119 1.243H4.25a1.125 1.125 0 01-1.12-1.243l1.264-12A1.125 1.125 0 015.513 7.5h12.974c.576 0 1.059.435 1.119 1.007zM8.625 10.5a.375.375 0 11-.75 0 .375.375 0 01.75 0zm7.5 0a.375.375 0 11-.75 0 .375.375 0 01.75 0z' })
  ])
};
const FireIconComp = { 
  render: () => h('svg', { xmlns: 'http://www.w3.org/2000/svg', fill: 'none', viewBox: '0 0 24 24', 'stroke-width': '1.5', stroke: 'currentColor' }, [
    h('path', { 'stroke-linecap': 'round', 'stroke-linejoin': 'round', d: 'M15.362 5.214A8.252 8.252 0 0112 21 8.25 8.25 0 016.038 7.047 8.287 8.287 0 009 9.601a8.983 8.983 0 013.361-6.867 8.21 8.21 0 003 2.48z' }),
    h('path', { 'stroke-linecap': 'round', 'stroke-linejoin': 'round', d: 'M12 18a3.75 3.75 0 00.495-7.467 5.99 5.99 0 00-1.925 3.546 5.974 5.974 0 01-2.133-1A3.75 3.75 0 0012 18z' })
  ])
};

const loading = ref(true);
const stats = ref([
   { label: "Today's Revenue", value: "$0.00", icon: markRaw(CurrencyDollarIconComp), gradient: 'grad-revenue card-safe-style' },
   { label: "Today's Orders", value: "0", icon: markRaw(ShoppingBagIconComp), gradient: 'grad-orders card-safe-style' },
   { label: "Active Orders", value: "0", icon: markRaw(FireIconComp), gradient: 'grad-active card-safe-style' }
]);

const series = ref([{ name: 'Revenue', data: [] }]);
const topItems = ref([]);

const chartOptions = ref({
   chart: { 
      type: 'area', 
      toolbar: { show: false },
      fontFamily: 'inherit',
      animations: { enabled: true, easing: 'easeinout', speed: 800 }
   },
   dataLabels: { enabled: false },
   stroke: { curve: 'smooth', width: 3, colors: ['#6366f1'] }, // Indigo-500
   fill: {
      type: 'gradient',
      gradient: {
         shadeIntensity: 1,
         opacityFrom: 0.6,
         opacityTo: 0.05,
         stops: [0, 90, 100],
         colorStops: [
             { offset: 0, color: '#818cf8', opacity: 0.6 },
             { offset: 100, color: '#c7d2fe', opacity: 0.1 }
         ]
      }
   },
   xaxis: {
      categories: [],
      axisBorder: { show: false },
      axisTicks: { show: false },
      labels: { 
          style: { colors: '#9ca3af', fontSize: '12px' } 
      }
   },
   yaxis: {
      labels: { 
          formatter: (val) => `$${val}`,
          style: { colors: '#9ca3af', fontSize: '12px' }
      }
   },
   grid: {
      borderColor: '#374151',
      strokeDashArray: 4,
      padding: { top: 10, right: 10, bottom: 10, left: 10 }
   },
   tooltip: {
      theme: 'dark',
      y: { formatter: (val) => `$${val}` }
   }
});

// Pie Chart Options
const pieOptions = ref({
   chart: { type: 'donut', fontFamily: 'inherit' },
   labels: [],
   colors: ['#6366f1', '#10b981', '#f59e0b'], // Indigo, Emerald, Amber
   legend: { position: 'bottom' },
   dataLabels: { enabled: false },
   plotOptions: {
     pie: {
       donut: {
         size: '70%',
         labels: {
            show: true,
            total: {
              show: true,
              label: 'Total',
              fontSize: '14px',
              fontFamily: 'inherit',
              color: '#9ca3af'
            }
         }
       }
     }
   }
});
const pieSeries = ref([]);
const aovStats = ref({ dineIn: 0, takeout: 0 });

// Update stats array to use component objects
// (Icons are now defined above)

// Update stats array to use component objects
stats.value[0].icon = markRaw(CurrencyDollarIconComp);
stats.value[1].icon = markRaw(ShoppingBagIconComp);
stats.value[2].icon = markRaw(FireIconComp);

const fetchData = async () => {
    loading.value = true;
    try {
        const token = localStorage.getItem('token');
        const headers = { Authorization: `Bearer ${token}` };

        // Fetch Summary
        const summaryRes = await axios.get('/api/orders/analytics/summary', { headers });
        const summary = summaryRes.data;
        
        stats.value[0].value = `$${summary.todayRevenue.toFixed(2)}`;
        stats.value[1].value = summary.todayOrders.toString();
        stats.value[2].value = summary.activeOrders.toString();

        // Fetch Trends
        const trendsRes = await axios.get('/api/orders/analytics/trends', { headers });
        const trends = trendsRes.data;
        chartOptions.value.xaxis.categories = trends.map(t => t.date);
        series.value[0].data = trends.map(t => t.revenue);

        // Fetch Top Items
        const itemsRes = await axios.get('/api/orders/analytics/top-items', { headers });
        const items = itemsRes.data;
        
        if (items.length > 0) {
           const maxQty = Math.max(...items.map(i => i.quantity));
           topItems.value = items.map(item => ({
              ...item,
              percentage: (item.quantity / maxQty) * 100
           }));
        } else {
           topItems.value = [];
        }

        // Fetch Distribution
        const distRes = await axios.get('/api/orders/analytics/distribution', { headers });
        const dist = distRes.data; // [{orderType, count, revenue}]
        
        pieOptions.value.labels = dist.map(d => d.orderType === 'DINE_IN' ? 'Dine-In' : 'Takeout');
        pieSeries.value = dist.map(d => d.revenue);
        
        // Calculate AOV
        const dineIn = dist.find(d => d.orderType === 'DINE_IN');
        const takeout = dist.find(d => d.orderType === 'TAKEOUT');
        
        aovStats.value.dineIn = dineIn && dineIn.count > 0 ? dineIn.revenue / dineIn.count : 0;
        aovStats.value.takeout = takeout && takeout.count > 0 ? takeout.revenue / takeout.count : 0;

    } catch (e) {
        console.error("Failed to fetch analytics", e);
    } finally {
        loading.value = false;
    }
}

onMounted(() => {
    fetchData();
});
</script>
