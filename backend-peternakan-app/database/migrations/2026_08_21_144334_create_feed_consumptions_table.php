<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
{
    Schema::create('feed_consumptions', function (Blueprint $table) {
        $table->id();
        $table->foreignId('cage_id')->constrained('cages')->cascadeOnDelete();
        $table->foreignId('feed_id')->constrained('feed_stocks')->cascadeOnDelete();
        $table->decimal('amount_used', 10, 2);
        $table->date('date');
        $table->foreignId('worker_id')->constrained('users')->cascadeOnDelete();
        $table->timestamps();
    });
}

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('feed_consumptions');
    }
};
