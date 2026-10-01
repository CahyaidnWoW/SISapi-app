<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\HealthRecordService;
use Illuminate\Http\Request;
use Exception;

class HealthRecordController extends Controller
{
    protected HealthRecordService $healthRecordService;

    public function __construct(HealthRecordService $healthRecordService)
    {
        $this->healthRecordService = $healthRecordService;
    }

    public function store(Request $request, string $tagNumber)
    {
        $validated = $request->validate([
            'type' => 'required|in:vaccination,treatment,emergency_report',
            'diagnosis' => 'nullable|string',
            'treatment' => 'nullable|string',
            'next_due_date' => 'nullable|date',
            'date' => 'required|date',
            'photo' => 'nullable|image|max:5120', 
        ]);

        try {
            $record = $this->healthRecordService->create(
                $tagNumber,
                $validated,
                $request->file('photo'),
                $request->user()->id
            );

            return response()->json($record, 201);
        } catch (Exception $e) {
            return response()->json(['message' => $e->getMessage()], 404);
        }
    }

    public function update(Request $request, int $id)
    {
        $validated = $request->validate([
            'diagnosis' => 'nullable|string',
            'treatment' => 'nullable|string',
        ]);

        try {
            $record = $this->healthRecordService->update($id, $validated, $request->user()->id);

            return response()->json($record);
        } catch (Exception $e) {
            return response()->json(['message' => $e->getMessage()], 404);
        }
    }

    public function byLivestock(string $tagNumber)
    {
        try {
            $records = $this->healthRecordService->getByLivestock($tagNumber);

            return response()->json($records);
        } catch (Exception $e) {
            return response()->json(['message' => $e->getMessage()], 404);
        }
    }

    public function due()
    {
        return response()->json($this->healthRecordService->getDueVaccinations());
    }
}