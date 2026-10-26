<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Livestock;
use App\Models\FeedConsumption;
use App\Models\HealthRecord;
use Barryvdh\DomPDF\Facade\Pdf;
use Carbon\Carbon;
use Illuminate\Http\Request;

class ReportController extends Controller
{
    private function prepareReportData(string $period): array
    {
        $now = Carbon::now();
        [$startDate, $endDate, $periodName] = match ($period) {
            'daily'   => [$now->copy()->startOfDay(), $now->copy()->endOfDay(), 'Harian'],
            'weekly'  => [$now->copy()->subDays(6)->startOfDay(), $now->copy()->endOfDay(), 'Mingguan (7 Hari)'],
            'monthly' => [$now->copy()->startOfMonth(), $now->copy()->endOfMonth(), 'Bulanan (' . $now->format('F Y') . ')'],
            default   => [$now->copy()->startOfDay(), $now->copy()->endOfDay(), 'Harian'],
        };

        $livestocks = Livestock::with(['cage', 'breed', 'weightLogs' => function ($q) {
            $q->orderBy('date', 'desc')->orderBy('id', 'desc');
        }])->whereIn('status', ['healthy', 'sick'])->get()->map(function ($item) {
            $latestLog = $item->weightLogs->first();
            $item->latest_weight = $latestLog ? (float)$latestLog->weight : (float)$item->initial_weight;
            return $item;
        });

        $feedConsumptions = FeedConsumption::with(['cage', 'feedStock'])
            ->whereBetween('date', [$startDate->toDateString(), $endDate->toDateString()])
            ->orderBy('date', 'desc')
            ->get();

        $healthRecords = HealthRecord::with(['livestock', 'handler'])
            ->whereBetween('date', [$startDate->toDateString(), $endDate->toDateString()])
            ->orderBy('date', 'desc')
            ->get();

        $summary = [
            'total_cattle'        => $livestocks->count(),
            'ready_to_sell'       => $livestocks->filter(fn($l) => $l->latest_weight >= 450)->count(),
            'sick_count'          => $livestocks->where('status', 'sick')->count(),
            'total_feed_consumed' => $feedConsumptions->sum('amount_used'),
        ];

        return [
            'periodName'       => $periodName,
            'startDate'        => $startDate->format('d/m/Y'),
            'endDate'          => $endDate->format('d/m/Y'),
            'generatedBy'      => auth()->user()?->name ?? 'Admin',
            'summary'          => $summary,
            'livestocks'       => $livestocks,
            'feedConsumptions' => $feedConsumptions,
            'healthRecords'    => $healthRecords,
        ];
    }

    public function exportPdf(Request $request)
    {
        $period = $request->get('period', 'daily');
        $data = $this->prepareReportData($period);

        $pdf = Pdf::loadView('reports.periodic', $data);
        $pdf->setPaper('A4', 'portrait');

        return $pdf->download("Laporan_Peternakan_{$period}_" . date('Ymd_His') . ".pdf");
    }
}
