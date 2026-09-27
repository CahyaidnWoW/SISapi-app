<?php

namespace App\Services;

use App\Models\Livestock;
use App\Models\Cage;
use Illuminate\Support\Facades\DB;

class LivestockService
{
    public function getAll()
    {
        return Livestock::with(['cage', 'breed'])->get();
    }

    public function findByTag(string $tagNumber)
    {
        $livestock = Livestock::with(['cage', 'breed'])
            ->where('tag_number', $tagNumber)
            ->first();

        if (!$livestock) {
            return null;
        }

        // Ambil berat terakhir dari weight_logs
        $latestWeight = $livestock->weightLogs()
            ->orderByDesc('date')
            ->first();

        // Ambil status vaksin terakhir (type = vaccination)
        $lastVaccine = $livestock->healthRecords()
            ->where('type', 'vaccination')
            ->orderByDesc('date')
            ->first();

        return [
            'livestock' => $livestock,
            'latest_weight' => $latestWeight->weight ?? $livestock->initial_weight,
            'latest_weight_date' => $latestWeight->date ?? null,
            'last_vaccine_date' => $lastVaccine->date ?? null,
            'next_vaccine_due' => $lastVaccine->next_due_date ?? null,
        ];
    }

    public function create(array $data)
    {
        return DB::transaction(function () use ($data) {
            $data['tag_number'] = $this->generateTagNumber();
            $data['status'] = 'healthy';

            $livestock = Livestock::create($data);

            // Update populasi kandang
            Cage::where('id', $data['cage_id'])->increment('current_population');

            return $livestock;
        });
    }

    protected function generateTagNumber(): string
    {
        $last = Livestock::orderByDesc('id')->first();
        $nextNumber = $last ? ((int) substr($last->tag_number, 3)) + 1 : 1;

        return 'SP-' . str_pad($nextNumber, 3, '0', STR_PAD_LEFT);
    }
}