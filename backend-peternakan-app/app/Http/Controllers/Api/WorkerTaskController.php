<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Cage;
use App\Models\FeedConsumption;
use App\Models\HealthRecord;
use Carbon\Carbon;
use Illuminate\Http\JsonResponse;

class WorkerTaskController extends Controller
{
    public function dailyTasks(): JsonResponse
    {
        $today = Carbon::today()->toDateString();
        $tasks = [];

        $cages = Cage::withCount(['livestocks' => function ($q) {
            $q->whereIn('status', ['healthy', 'sick']);
        }])->get();

        foreach ($cages as $cage) {
            $feedToday = FeedConsumption::with(['feedStock', 'worker'])
                ->where('cage_id', $cage->id)
                ->whereDate('date', $today)
                ->latest()
                ->first();

            if ($feedToday) {
                $workerName = $feedToday->worker ? $feedToday->worker->name : 'Pekerja';
                $feedName = $feedToday->feedStock ? $feedToday->feedStock->feed_name : 'Pakan';
                $unit = $feedToday->feedStock ? ($feedToday->feedStock->unit ?? 'kg') : 'kg';
                $amount = (float)$feedToday->amount_used;
                $time = $feedToday->created_at ? $feedToday->created_at->format('H:i') : '';

                $tasks[] = [
                    'id' => 'feed_cage_' . $cage->id,
                    'type' => 'feed',
                    'cage_id' => $cage->id,
                    'tag_number' => null,
                    'title' => 'Pakan ' . $cage->name,
                    'is_completed' => true,
                    'completed_by' => $workerName,
                    'completed_at' => $time,
                    'detail' => 'Selesai oleh ' . $workerName . ($time ? ' (' . $time . ')' : '') . ' • ' . $feedName . ' ' . $amount . ' ' . $unit,
                    'action' => 'none'
                ];
            } else {
                $livestockCount = $cage->livestocks_count;
                $tasks[] = [
                    'id' => 'feed_cage_' . $cage->id,
                    'type' => 'feed',
                    'cage_id' => $cage->id,
                    'tag_number' => null,
                    'title' => 'Pakan ' . $cage->name,
                    'is_completed' => false,
                    'completed_by' => null,
                    'completed_at' => null,
                    'detail' => 'Belum diberi pakan hari ini (' . $livestockCount . ' ekor) • Klik untuk input',
                    'action' => 'feed_input'
                ];
            }
        }

        $emergencies = HealthRecord::with(['livestock.cage'])
            ->where('type', 'emergency_report')
            ->whereNull('handled_by')
            ->latest('date')
            ->take(5)
            ->get();

        foreach ($emergencies as $rec) {
            $tag = $rec->livestock ? $rec->livestock->tag_number : '—';
            $cageName = $rec->livestock && $rec->livestock->cage ? $rec->livestock->cage->name : 'Kandang';
            $diag = $rec->diagnosis ?: 'Laporan gejala sakit';

            $tasks[] = [
                'id' => 'emergency_' . $rec->id,
                'type' => 'monitor',
                'cage_id' => $rec->livestock ? $rec->livestock->cage_id : null,
                'tag_number' => $tag,
                'title' => 'Pantau: Sapi ' . $tag,
                'is_completed' => false,
                'completed_by' => null,
                'completed_at' => null,
                'detail' => $diag . ' (' . $cageName . ') • Klik untuk profil',
                'action' => 'scan_profile'
            ];
        }

        $total = count($tasks);
        $completed = count(array_filter($tasks, fn($t) => $t['is_completed']));

        return response()->json([
            'date' => $today,
            'total_tasks' => $total,
            'completed_tasks' => $completed,
            'tasks' => $tasks
        ]);
    }
}
