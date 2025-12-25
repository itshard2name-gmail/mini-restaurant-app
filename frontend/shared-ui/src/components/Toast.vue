<script setup>
import { computed } from 'vue';
import { CheckCircle, AlertCircle, AlertTriangle, Info, X } from 'lucide-vue-next';

const props = defineProps({
  show: Boolean,
  title: {
    type: String,
    default: ''
  },
  message: String,
  type: {
    type: String,
    default: 'success',
    validator: (value) => ['success', 'error', 'warning', 'info'].includes(value)
  }
});

const emit = defineEmits(['close']);

const isSuccess = computed(() => props.type === 'success');
const isError = computed(() => props.type === 'error');
const isWarning = computed(() => props.type === 'warning');
const isInfo = computed(() => props.type === 'info');

const mainBorderColorClass = computed(() => {
  if (isSuccess.value) return 'border-emerald-500';
  if (isError.value) return 'border-rose-500';
  if (isWarning.value) return 'border-amber-500';
  if (isInfo.value) return 'border-blue-500';
  return 'border-emerald-500';
});
</script>

<template>
  <Transition
    enter-active-class="transform ease-out duration-300 transition"
    enter-from-class="translate-y-10 opacity-0 sm:translate-y-0 sm:translate-x-10"
    enter-to-class="translate-y-0 opacity-100 sm:translate-x-0"
    leave-active-class="transition ease-in duration-200"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0"
  >
    <div 
        v-if="show" 
        class="fixed z-[10000] flex w-[90%] max-w-sm bg-white border shadow-2xl rounded-xl overflow-hidden pointer-events-auto transition-all duration-300
               top-6 left-0 right-0 mx-auto
               md:top-6 md:right-6 md:left-auto md:mx-0 md:w-96"
        :class="mainBorderColorClass"
    >
      <!-- Colorful Left Strip (Thicker) -->
      <div 
        class="w-3 flex-shrink-0"
        :class="[
            isSuccess ? 'bg-emerald-500' : '',
            isError ? 'bg-rose-500' : '',
            isWarning ? 'bg-amber-500' : '',
            isInfo ? 'bg-blue-500' : ''
        ]"
      ></div>

      <div class="flex-1 p-5 pl-4 flex items-start">
        <!-- Icon Section (Larger Circle) -->
        <div class="flex-shrink-0 pt-0.5">
           <!-- Using Filled Icons for stronger semantic visual -->
           <CheckCircle v-if="isSuccess" class="h-8 w-8 text-emerald-500 fill-emerald-50" />
           <AlertCircle v-else-if="isError" class="h-8 w-8 text-rose-500 fill-rose-50" />
           <AlertTriangle v-else-if="isWarning" class="h-8 w-8 text-amber-500 fill-amber-50" />
           <Info v-else class="h-8 w-8 text-blue-500 fill-blue-50" />
        </div>

        <!-- Content Section -->
        <div class="ml-4 w-0 flex-1">
          <p v-if="title" class="text-base font-bold text-slate-900 leading-tight mb-1">
            {{ title }}
          </p>
          <p class="text-sm text-slate-500 font-medium leading-relaxed">
            {{ message }}
          </p>
        </div>
      </div>

      <!-- Close Button (Absolute Top-Right) -->
      <button 
        @click="$emit('close')" 
        class="absolute top-2 right-2 text-slate-400 hover:text-slate-600 p-1 rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-slate-100"
      >
        <span class="sr-only">Close</span>
        <X class="h-4 w-4" />
      </button>
    </div>
  </Transition>
</template>
