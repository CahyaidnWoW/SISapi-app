<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Livestock;
use App\Models\WeightLog;
use App\Models\HealthRecord;
use App\Models\FeedStock;
use Carbon\Carbon;
use Illuminate\Http\JsonResponse;

class DashboardController extends Controller
{
    public function summary(): JsonResponse
    {
        $activeLivestocks = Livestock::whereIn('status', ['healthy', 'sick'])
            ->with(['weightLogs' => function ($q) {
                $q->orderBy('date', 'desc')->orderBy('id', 'desc');
            }])
            ->get();

        $totalCattle = $activeLivestocks->count();

        $sickCount = Livestock::where('status', 'sick')->count();

        $readyToSell = 0;
        $adgList = [];

        foreach ($activeLivestocks as $livestock) {
            $latestLog = $livestock->weightLogs->first();
            $currentWeight = $latestLog ? (float)$latestLog->weight : (float)$livestock->initial_weight;

            if ($currentWeight >= 450) {
                $readyToSell++;
            }

            if ($latestLog) {
                $days = Carbon::parse($livestock->entry_date)->diffInDays(Carbon::parse($latestLog->date));
                if ($days > 0) {
                    $gain = $currentWeight - (float)$livestock->initial_weight;
                    $adgList[] = $gain / $days;
                }
            }
        }

        $avgAdg = count($adgList) > 0 ? round(array_sum($adgList) / count($adgList), 2) : 0.0;
        
        $criticalFeedCount = FeedStock::withSum('batches as total_stock', 'quantity_remaining')
            ->get()
            ->filter(fn($feed) => ($feed->total_stock ?? 0) <= 50)
            ->count();

        $upcomingVaccinations = HealthRecord::where('type', 'vaccination')
            ->whereNotNull('next_due_date')
            ->whereBetween('next_due_date', [Carbon::today(), Carbon::today()->addDays(7)])
            ->with('livestock')
            ->orderBy('next_due_date', 'asc')
            ->limit(5)
            ->get()
            ->map(function ($record) {
                return [
                    'id'             => $record->id,
                    'cattle_tag'     => $record->livestock ? $record->livestock->tag_number : '-',
                    'treatment_type' => $record->treatment ?? $record->diagnosis ?? 'Vaksinasi',
                    'scheduled_date' => $record->next_due_date,
                    'status'         => 'Pending'
                ];
            });

        return response()->json([
            'status' => 'success',
            'data' => [
                'total_cattle'        => $totalCattle,
                'ready_to_sell'       => $readyToSell,
                'sick_cattle'         => $sickCount,
                'critical_feed_count' => $criticalFeedCount,
                'avg_adg_30d'         => $avgAdg,
                'upcoming_medicals'   => $upcomingVaccinations
            ]
        ]);
    }
    public function growthTrend(): JsonResponse
    {
        $months = 6;
        $startDate = Carbon::now()->subMonths($months - 1)->startOfMonth();
        $growthData = [];

        for ($i = 0; $i < $months; $i++) {
            $monthDate = $startDate->copy()->addMonths($i);
            $monthEnd = $monthDate->copy()->endOfMonth();

            $avgWeight = WeightLog::whereBetween('date', [$monthDate->toDateString(), $monthEnd->toDateString()])
                ->avg('weight');

            if (!$avgWeight || $avgWeight == 0) {
                $avgWeight = Livestock::where('entry_date', '<=', $monthEnd->toDateString())->avg('initial_weight') ?? 0;
            }


            $growthData[] = [
                'period'     => $monthDate->format('M Y'),
                'avg_weight' => round((float)$avgWeight, 1),
                'avg_adg'    => 0.85
            ];
        }

        return response()->json([
            'status' => 'success',
            'data' => $growthData
        ]);
    }

    public function populationTrend(): JsonResponse
    {
        $months = 6;
        $startDate = Carbon::now()->subMonths($months - 1)->startOfMonth();
        $data = [];

        for ($i = 0; $i < $months; $i++) {
            $monthDate = $startDate->copy()->addMonths($i);
            $monthEnd = $monthDate->copy()->endOfMonth();

            $count = Livestock::where('entry_date', '<=', $monthEnd->toDateString())
                ->where(function ($query) use ($monthEnd) {
                    $query->whereIn('status', ['healthy', 'sick'])
                          ->orWhere('updated_at', '>', $monthEnd->endOfDay());
                })
                ->count();

            $data[] = [
                'period' => $monthDate->format('M Y'),
                'count'  => $count,
            ];
        }

        return response()->json([
            'status' => 'success',
            'data' => $data
        ]);
    }
}
