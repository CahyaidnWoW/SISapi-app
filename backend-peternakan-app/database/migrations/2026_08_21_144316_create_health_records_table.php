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
    Schema::create('health_records', function (Blueprint $table) {
        $table->id();
        $table->foreignId('livestock_id')->constrained('livestocks')->cascadeOnDelete();
        $table->enum('type', ['vaccination', 'treatment', 'emergency_report']);
        $table->text('diagnosis')->nullable();
        $table->text('treatment')->nullable();
        $table->string('photo_path')->nullable();
        $table->date('next_due_date')->nullable();
        $table->foreignId('handled_by')->nullable()->constrained('users')->nullOnDelete();
        $table->date('date');
        $table->timestamps();
    });
}

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('health_records');
    }
};
